package com.example.pfsm.viewModels




import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.datautil.resolveDailyLimitForDate
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.DailyLimitEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.session.SessionManager
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.TransactionRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class MonthBarData(
    val month: Int,          // 1..12
    val label: String,       // "Jan", "Feb", ...
    val expense: Double,
    val income: Double
)

data class DayBarData(
    val day: Int,             // 1..31
    val amount: Double
)

data class CategorySpendItem(
    val categoryId: Int?,
    val name: String,
    val icon: String,
    val amount: Double,
    val percentOfExpense: Int
)

data class DashboardUiState(
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedDay: Int? = null,

    val monthlyData: List<MonthBarData> = emptyList(),

    val yearlyIncome: Double = 0.0,
    val yearlyExpense: Double = 0.0,
    val yearlyBalance: Double = 0.0,
    val yearlySavingsPercent: Int = 0,

    val topCategories: List<CategorySpendItem> = emptyList(),
    val dailyBreakdown: List<DayBarData> = emptyList(),
    val monthTotalSpend: Double = 0.0,
    val selectedDayTransactions: List<TransactionEntity> = emptyList(),


    val averageDailySpend: Double = 0.0,
    val highestSpendingDay: DayBarData? = null,
    val daysWithinLimit: Int = 0,
    val daysOverLimit: Int = 0,
    val hasLimitDataForMonth: Boolean = false,

    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val selectedYearFlow = MutableStateFlow(LocalDate.now().year)
    private val selectedMonthFlow = MutableStateFlow(YearMonth.now())
    private val selectedDayFlow = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .flatMapLatest { userId ->
                    combine(
                        transactionRepository.getAllForUser(userId),
                        categoryRepository.getAllForUser(userId),
                        dailyLimitRepository.getHistoryForUser(userId)
                    ) { allTransactions, allCategories, limitHistory ->
                        Triple(allTransactions, allCategories, limitHistory)
                    }.flatMapLatest { (allTransactions, allCategories, limitHistory) ->
                        combine(
                            selectedYearFlow,
                            selectedMonthFlow,
                            selectedDayFlow
                        ) { year, month, day -> Triple(year, month, day) }
                            .map { (year, month, day) ->
                                buildState(allTransactions, allCategories, limitHistory, year, month, day)
                            }
                    }
                }
                .collectLatest { state -> _uiState.value = state }
        }
    }

    private fun buildState(
        allTransactions: List<TransactionEntity>,
        allCategories: List<CategoryEntity>,
        limitHistory: List<DailyLimitEntity>,
        year: Int,
        month: YearMonth,
        selectedDay: Int?
    ): DashboardUiState {
        val categoriesById = allCategories.associateBy { it.id }
        val yearPrefix = "$year-"

        val yearTransactions = allTransactions.filter { it.transactionDate.startsWith(yearPrefix) }
        val yearExpenseTxns = yearTransactions.filter { it.type == "debit" }
        val yearIncome = yearTransactions.filter { it.type == "credit" }.sumOf { it.amount }
        val yearExpense = yearExpenseTxns.sumOf { it.amount }
        val yearBalance = yearIncome - yearExpense
        val savingsPercent = if (yearIncome > 0) ((yearBalance / yearIncome) * 100).roundToInt() else 0

        val monthlyData = (1..12).map { m ->
            val monthPrefix = "$year-${m.toString().padStart(2, '0')}"
            val monthTxns = yearTransactions.filter { it.transactionDate.startsWith(monthPrefix) }
            MonthBarData(
                month = m,
                label = java.time.Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                expense = monthTxns.filter { it.type == "debit" }.sumOf { it.amount },
                income = monthTxns.filter { it.type == "credit" }.sumOf { it.amount }
            )
        }

        val topCategories = yearExpenseTxns
            .groupBy { it.categoryId }
            .map { (categoryId, txns) ->
                val category = categoryId?.let { categoriesById[it] }
                val amount = txns.sumOf { it.amount }
                CategorySpendItem(
                    categoryId = categoryId,
                    name = category?.name ?: "Uncategorized",
                    icon = category?.icon ?: "category",
                    amount = amount,
                    percentOfExpense = if (yearExpense > 0) ((amount / yearExpense) * 100).roundToInt() else 0
                )
            }
            .sortedByDescending { it.amount }
            .take(6)

        val monthPrefix = month.toString() // YearMonth.toString() = "yyyy-MM"
        val monthExpenseTxns = allTransactions.filter {
            it.type == "debit" && it.transactionDate.startsWith(monthPrefix)
        }
        val monthTotalSpend = monthExpenseTxns.sumOf { it.amount }

        val dailyBreakdown = (1..month.lengthOfMonth()).map { day ->
            val dateStr = month.atDay(day).toString()
            val amount = monthExpenseTxns.filter { it.transactionDate == dateStr }.sumOf { it.amount }
            DayBarData(day = day, amount = amount)
        }

        val today = LocalDate.now()
        val elapsedDays = if (month == YearMonth.from(today)) today.dayOfMonth else month.lengthOfMonth()
        val averageDailySpend = if (elapsedDays > 0) monthTotalSpend / elapsedDays else 0.0
        val highestSpendingDay = dailyBreakdown.filter { it.amount > 0 }.maxByOrNull { it.amount }


        var withinCount = 0
        var overCount = 0
        for (day in 1..month.lengthOfMonth()) {
            val date = month.atDay(day)
            if (date.isAfter(today)) continue
            val limitThatDay = resolveDailyLimitForDate(limitHistory, date)
            if (limitThatDay <= 0) continue
            val spentThatDay = dailyBreakdown.getOrNull(day - 1)?.amount ?: 0.0
            if (spentThatDay > limitThatDay) overCount++ else withinCount++
        }

        val selectedDayTransactions = if (selectedDay != null) {
            val dateStr = month.atDay(selectedDay).toString()
            allTransactions.filter { it.transactionDate == dateStr }.sortedByDescending { it.createdAt }
        } else {
            emptyList()
        }

        return DashboardUiState(
            selectedYear = year,
            selectedMonth = month,
            selectedDay = selectedDay,
            monthlyData = monthlyData,
            yearlyIncome = yearIncome,
            yearlyExpense = yearExpense,
            yearlyBalance = yearBalance,
            yearlySavingsPercent = savingsPercent,
            topCategories = topCategories,
            dailyBreakdown = dailyBreakdown,
            monthTotalSpend = monthTotalSpend,
            selectedDayTransactions = selectedDayTransactions,
            averageDailySpend = averageDailySpend,
            highestSpendingDay = highestSpendingDay,
            daysWithinLimit = withinCount,
            daysOverLimit = overCount,
            hasLimitDataForMonth = (withinCount + overCount) > 0,
            isLoading = false
        )
    }

    fun onYearChanged(year: Int) {
        selectedYearFlow.value = year
        selectedDayFlow.value = null

        selectedMonthFlow.update { currentMonth ->
            YearMonth.of(year, currentMonth.monthValue)
        }
    }

    fun onMonthSelected(month: YearMonth) {
        selectedMonthFlow.value = month
        selectedDayFlow.value = null
        if (month.year != selectedYearFlow.value) {
            selectedYearFlow.value = month.year
        }
    }

    fun onMonthBarClicked(monthNumber: Int) {
        onMonthSelected(YearMonth.of(selectedYearFlow.value, monthNumber))
    }

    fun onDayBarClicked(day: Int) {
        selectedDayFlow.update { if (it == day) null else day }
    }
}