package com.example.pfsm.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.entities.BudgetEntity
import com.example.pfsm.data.entities.CategoryEntity
import java.time.YearMonth
import com.example.pfsm.data.repository.BudgetRepository
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddBudgetUiState(
    val currentUserId: Int? = null,

    val budgetType: String = "overall",   // "overall" or "category"
    val name: String = "",
    val amountText: String = "",
    val periodType: String = "monthly",   // "monthly" or "yearly"
    val selectedCategoryIds: Set<Int> = emptySet(),

    val expenseCategories: List<CategoryEntity> = emptyList(),

    val nameError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,

    val isSaved: Boolean = false,

    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = LocalDate.now().year,
)

class AddBudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBudgetUiState())
    val uiState: StateFlow<AddBudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    _uiState.update { it.copy(currentUserId = userId) }
                    // Budgets only ever track spending, so only expense categories are relevant here.
                    categoryRepository.getByType(userId, "expense").collectLatest { categories ->
                        _uiState.update { it.copy(expenseCategories = categories) }
                    }
                }
        }
    }

    fun onBudgetTypeChanged(type: String) {
        _uiState.update { it.copy(budgetType = type, selectedCategoryIds = emptySet(), categoryError = null) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onAmountChanged(text: String) {
        val filtered = text.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && text.indexOf('.') == index)
        }
        _uiState.update { it.copy(amountText = filtered, amountError = null) }
    }

    fun onPeriodTypeChanged(type: String) {
        _uiState.update { it.copy(periodType = type) }
    }

    fun onCategoryToggled(categoryId: Int) {
        _uiState.update {
            val current = it.selectedCategoryIds
            val updated = if (categoryId in current) current - categoryId else current + categoryId
            it.copy(selectedCategoryIds = updated, categoryError = null)
        }
    }

    fun onMonthSelected(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month) }
    }

    fun onYearSelected(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun save() {
        val state = _uiState.value
        val userId = state.currentUserId ?: return

        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Enter a budget name") }
            hasError = true
        }
        val amount = state.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            hasError = true
        }
        if (state.budgetType == "category" && state.selectedCategoryIds.isEmpty()) {
            _uiState.update { it.copy(categoryError = "Select at least one category") }
            hasError = true
        }
        if (hasError || amount == null) return


        val budget = BudgetEntity(
            userId = userId,
            name = state.name.trim(),
            amount = amount,
            periodType = state.periodType,
            month = if (state.periodType == "monthly") state.selectedMonth.monthValue else null,
            year = if (state.periodType == "monthly") state.selectedMonth.year else state.selectedYear,
            isOverall = state.budgetType == "overall"
        )

        val categoryIds = if (state.budgetType == "category") state.selectedCategoryIds.toList() else emptyList()

        viewModelScope.launch {
            budgetRepository.createBudget(budget, categoryIds)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}