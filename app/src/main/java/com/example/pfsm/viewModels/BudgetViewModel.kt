package com.example.pfsm.viewModels



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.dao.BudgetWithCategories
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.repository.BudgetRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.session.SessionManager
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

data class BudgetCardUiModel(
    val id: Int,
    val name: String,
    val amount: Double,
    val spent: Double,
    val remaining: Double,
    val percentUsed: Int,
    val isOverall: Boolean,
    val categories: List<CategoryEntity>
)

data class BudgetUiState(
    val periodType: String = "monthly",       // "monthly" or "yearly"
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedYear: Int = LocalDate.now().year,
    val budgetCards: List<BudgetCardUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val periodTypeFlow = MutableStateFlow("monthly")
    private val selectedMonthFlow = MutableStateFlow(YearMonth.now())
    private val selectedYearFlow = MutableStateFlow(LocalDate.now().year)

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .flatMapLatest { userId ->
                    combine(periodTypeFlow, selectedMonthFlow, selectedYearFlow) { type, month, year ->
                        Triple(type, month, year)
                    }.flatMapLatest { (type, month, year) ->
                        val queryYear = if (type == "monthly") month.year else year
                        val queryMonth = if (type == "monthly") month.monthValue else 0

                        combine(
                            budgetRepository.getBudgetsWithCategories(userId, queryYear, type, queryMonth),
                            transactionRepository.getAllForUser(userId)
                        ) { budgets, allTransactions ->
                            buildCards(budgets, allTransactions, type, month, year)
                        }
                    }
                }
                .collectLatest { cards ->
                    _uiState.update { it.copy(budgetCards = cards, isLoading = false) }
                }
        }
    }

    private fun buildCards(
        budgets: List<BudgetWithCategories>,
        allTransactions: List<TransactionEntity>,
        periodType: String,
        month: YearMonth,
        year: Int
    ): List<BudgetCardUiModel> {
        val (start, end) = if (periodType == "monthly") {
            month.atDay(1).format(dateFmt) to month.atEndOfMonth().format(dateFmt)
        } else {
            LocalDate.of(year, 1, 1).format(dateFmt) to LocalDate.of(year, 12, 31).format(dateFmt)
        }

        return budgets.map { budgetWithCategories ->
            val budget = budgetWithCategories.budget
            val categoryIds = budgetWithCategories.categories.map { it.id }.toSet()

            val spent = allTransactions
                .filter { txn ->
                    txn.type == "debit" &&
                            txn.transactionDate in start..end &&
                            (budget.isOverall || txn.categoryId in categoryIds)
                }
                .sumOf { it.amount }

            val remaining = budget.amount - spent
            val percent = if (budget.amount > 0) ((spent / budget.amount) * 100).roundToInt() else 0

            BudgetCardUiModel(
                id = budget.id,
                name = budget.name,
                amount = budget.amount,
                spent = spent,
                remaining = remaining,
                percentUsed = percent,
                isOverall = budget.isOverall,
                categories = budgetWithCategories.categories
            )
        }
    }

    fun onPeriodTypeChanged(type: String) {
        periodTypeFlow.value = type
        _uiState.update { it.copy(periodType = type) }
    }

    fun onMonthChanged(newMonth: YearMonth) {
        selectedMonthFlow.value = newMonth
        _uiState.update { it.copy(selectedMonth = newMonth) }
    }

    fun onYearChanged(newYear: Int) {
        selectedYearFlow.value = newYear
        _uiState.update { it.copy(selectedYear = newYear) }
    }
}