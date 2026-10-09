package com.example.pfsm.viewModels



import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.dao.BudgetWithCategories
import com.example.pfsm.data.entities.BudgetEntity
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity

import com.example.pfsm.data.repository.BudgetRepository
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.session.SessionManager
import com.example.pfsm.ui.theme.util.MAX_AMOUNT
import com.example.pfsm.ui.theme.util.MAX_TEXT_LENGTH
import com.example.pfsm.ui.theme.util.toEditableAmountString
import com.example.pfsm.ui.theme.util.toReadableAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


const val UNCATEGORIZED_ID = -1

data class CategoryBreakdownItem(
    val categoryId: Int,   // UNCATEGORIZED_ID for transactions with no category
    val name: String,
    val icon: String,
    val spent: Double,
    val percentOfSpent: Int
)

data class BudgetDetailUiState(
    val budgetId: Int = 0,
    val name: String = "",
    val amount: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val percentUsed: Int = 0,
    val isOverall: Boolean = true,
    val periodLabel: String = "",

    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val selectedCategoryId: Int? = null,
    val visibleTransactions: List<TransactionEntity> = emptyList(),

    //  Edit mode
    val isEditing: Boolean = false,
    val editName: String = "",
    val editAmountText: String = "",
    val editSelectedCategoryIds: Set<Int> = emptySet(),
    val editableExpenseCategories: List<CategoryEntity> = emptyList(),
    val editNameError: String? = null,
    val editAmountError: String? = null,
    val editCategoryError: String? = null,

    val notFound: Boolean = false,
    val isLoading: Boolean = true,

    val isDeleting: Boolean = false,

    val periodType: String = "monthly",
    val editMonth: YearMonth = YearMonth.now(),
    val editYear: Int = LocalDate.now().year,
)

class BudgetDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val budgetId: Int = checkNotNull(savedStateHandle["budgetId"])

    private val _uiState = MutableStateFlow(BudgetDetailUiState(budgetId = budgetId))
    val uiState: StateFlow<BudgetDetailUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE


    private var latestInScopeTransactions: List<TransactionEntity> = emptyList()

    private var currentBudget: BudgetEntity? = null
    private var currentLinkedCategoryIds: Set<Int> = emptySet()

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .flatMapLatest { userId ->
                    combine(
                        budgetRepository.getBudgetWithCategoriesById(budgetId, userId),
                        transactionRepository.getAllForUser(userId),
                        categoryRepository.getAllForUser(userId)
                    ) { budgetWithCategories, allTransactions, allCategories ->
                        Triple(budgetWithCategories, allTransactions, allCategories)
                    }
                }
                .collectLatest { (budgetWithCategories, allTransactions, allCategories) ->
                    if (budgetWithCategories == null) {
                        _uiState.update { it.copy(notFound = true, isLoading = false) }
                        return@collectLatest
                    }

                    currentBudget = budgetWithCategories.budget
                    currentLinkedCategoryIds = budgetWithCategories.categories.map { it.id }.toSet()

                    val expenseCategories = allCategories.filter { it.type == "expense" }
                    val (state, inScope) = buildState(budgetWithCategories, allTransactions, allCategories)
                    latestInScopeTransactions = inScope

                    _uiState.update { current ->
                        state.copy(
                            selectedCategoryId = current.selectedCategoryId,
                            visibleTransactions = filterByCategory(inScope, current.selectedCategoryId),
                            isEditing = current.isEditing,
                            editName = current.editName,
                            editAmountText = current.editAmountText,
                            editSelectedCategoryIds = current.editSelectedCategoryIds,
                            editableExpenseCategories = expenseCategories,
                            editNameError = current.editNameError,
                            editAmountError = current.editAmountError,
                            editCategoryError = current.editCategoryError,
                            editMonth = current.editMonth,
                            editYear = current.editYear,
                        )
                    }
                }
        }
    }

    private fun buildState(
        budgetWithCategories: BudgetWithCategories,
        allTransactions: List<TransactionEntity>,
        allCategories: List<CategoryEntity>
    ): Pair<BudgetDetailUiState, List<TransactionEntity>> {
        val budget = budgetWithCategories.budget
        val linkedCategoryIds = budgetWithCategories.categories.map { it.id }.toSet()
        val categoriesById = allCategories.associateBy { it.id }

        val (start, end, periodLabel) = if (budget.periodType == "monthly") {
            val month = YearMonth.of(budget.year, budget.month ?: 1)
            Triple(
                month.atDay(1).format(dateFmt),
                month.atEndOfMonth().format(dateFmt),
                month.format(DateTimeFormatter.ofPattern("MMM yyyy"))
            )
        } else {
            Triple(
                LocalDate.of(budget.year, 1, 1).format(dateFmt),
                LocalDate.of(budget.year, 12, 31).format(dateFmt),
                budget.year.toString()
            )
        }

        val inScope = allTransactions.filter { txn ->
            txn.type == "debit" &&
                    txn.transactionDate in start..end &&
                    (budget.isOverall || txn.categoryId in linkedCategoryIds)
        }

        val spent = inScope.sumOf { it.amount }
        val remaining = budget.amount - spent
        val percent = if (budget.amount > 0) ((spent / budget.amount) * 100).roundToInt() else 0

        val breakdown = inScope
            .groupBy { it.categoryId ?: UNCATEGORIZED_ID }
            .map { (categoryId, txns) ->
                val category = if (categoryId == UNCATEGORIZED_ID) null else categoriesById[categoryId]
                val categorySpent = txns.sumOf { it.amount }
                CategoryBreakdownItem(
                    categoryId = categoryId,
                    name = category?.name ?: "Uncategorized",
                    icon = category?.icon ?: "category",
                    spent = categorySpent,
                    percentOfSpent = if (spent > 0) ((categorySpent / spent) * 100).roundToInt() else 0
                )
            }
            .sortedByDescending { it.spent }

        val state = BudgetDetailUiState(
            budgetId = budget.id,
            name = budget.name,
            amount = budget.amount,
            spent = spent,
            remaining = remaining,
            percentUsed = percent,
            isOverall = budget.isOverall,
            periodLabel = periodLabel,
            categoryBreakdown = breakdown,
            visibleTransactions = inScope.sortedByDescending { it.transactionDate },
            isLoading = false,
            periodType = budget.periodType,
        )
        return state to inScope
    }

    private fun filterByCategory(transactions: List<TransactionEntity>, selectedId: Int?): List<TransactionEntity> {
        val sorted = transactions.sortedByDescending { it.transactionDate }
        return when (selectedId) {
            null -> sorted
            UNCATEGORIZED_ID -> sorted.filter { it.categoryId == null }
            else -> sorted.filter { it.categoryId == selectedId }
        }
    }


    fun onCategorySelected(categoryId: Int) {
        _uiState.update { current ->
            val newSelection = if (current.selectedCategoryId == categoryId) null else categoryId
            current.copy(
                selectedCategoryId = newSelection,
                visibleTransactions = filterByCategory(latestInScopeTransactions, newSelection)
            )
        }
    }

    // EDIT MODE
    fun startEdit() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isEditing = true,
                editName = state.name,
                editAmountText = if (state.amount > 0) state.amount.toEditableAmountString() else "",
                editSelectedCategoryIds = currentLinkedCategoryIds,
                editNameError = null,
                editAmountError = null,
                editCategoryError = null,
                editMonth = YearMonth.of(currentBudget?.year ?: LocalDate.now().year, currentBudget?.month ?: 1),
                editYear = currentBudget?.year ?: LocalDate.now().year,
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(isEditing = false) }
    }

    fun onEditNameChanged(name: String) {
        _uiState.update { it.copy(editName = name.take(MAX_TEXT_LENGTH), editNameError = null) }
    }

    fun onEditAmountChanged(text: String) {
        val filtered = text.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && text.indexOf('.') == index)
        }
        _uiState.update { it.copy(editAmountText = filtered, editAmountError = null) }
    }

    fun onEditCategoryToggled(categoryId: Int) {
        _uiState.update {
            val current = it.editSelectedCategoryIds
            val updated = if (categoryId in current) current - categoryId else current + categoryId
            it.copy(editSelectedCategoryIds = updated, editCategoryError = null)
        }
    }

    fun onEditMonthSelected(month: YearMonth) {
        _uiState.update { it.copy(editMonth = month) }
    }

    fun onEditYearSelected(year: Int) {
        _uiState.update { it.copy(editYear = year) }
    }

    fun saveEdit() {
        val state = _uiState.value
        val existingBudget = currentBudget ?: return

        var hasError = false
        if (state.editName.isBlank()) {
            _uiState.update { it.copy(editNameError = "Enter a budget name") }
            hasError = true
        }
        val amount = state.editAmountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0 || amount > MAX_AMOUNT) {
            _uiState.update { it.copy(editAmountError = "Enter a valid amount (max ₹${MAX_AMOUNT.toReadableAmount()})") }
            hasError = true
        }
        if (!existingBudget.isOverall && state.editSelectedCategoryIds.isEmpty()) {
            _uiState.update { it.copy(editCategoryError = "Select at least one category") }
            hasError = true
        }
        if (hasError || amount == null) return

        val isMonthly = existingBudget.periodType == "monthly"
        val updatedBudget = existingBudget.copy(
            name = state.editName.trim(),
            amount = amount,
            month = if (isMonthly) state.editMonth.monthValue else null,
            year = if (isMonthly) state.editMonth.year else state.editYear
        )
        val categoryIds = if (existingBudget.isOverall) emptyList() else state.editSelectedCategoryIds.toList()

        viewModelScope.launch {
            budgetRepository.updateBudget(updatedBudget, categoryIds)
            _uiState.update { it.copy(isEditing = false) }
        }
    }


    fun startDeleteBudget(){
        _uiState.update {
            it.copy(isDeleting = true)
        }

    }
    fun cancelDeleteBudget(){
        _uiState.update { it.copy(isDeleting = false) }
    }
    fun confirmDeleteBudget(){
        val existingBudget = currentBudget ?: return
        viewModelScope.launch {
            budgetRepository.delete(existingBudget)
        }
        _uiState.update { it.copy(isDeleting = false) }
    }
}
