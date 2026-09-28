package com.example.pfsm.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.datautil.DailyTotal
import com.example.pfsm.data.datautil.resolveDailyLimitForDate
import com.example.pfsm.data.entities.DailyLimitEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.entities.UserEntity

import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.repository.UserRepository
import com.example.pfsm.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
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
import kotlin.collections.associate

data class CalendarDay(
    val date: LocalDate,
    val isOverLimit: Boolean
)

data class HomeUiState(
    val user: UserEntity? = null,
    val selectedMonth: YearMonth = YearMonth.now(),

    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyBalance: Double = 0.0,
    val incomePercent: Int = 0,
    val expensePercent: Int = 0,
    val lastMonthBalance: Double = 0.0,

    val todayDailyLimit: Double = 0.0, // the limit in effect TODAY specifically
    val todaySpend: Double = 0.0,
    val isDailyLimitExceeded: Boolean = false,

    val recentTransactions: List<TransactionEntity> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),

    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateTransactions: List<TransactionEntity> = emptyList(),

    val isLoading: Boolean = true
)

class HomeViewModel(
    private val userRepository: UserRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val transactionRepository: TransactionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val selectedMonthFlow = MutableStateFlow(YearMonth.now())
    private val selectedDateFlow = MutableStateFlow(LocalDate.now())

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    coroutineScope {
                        launch { observeUser(userId) }
                        launch { observeRecentTransactions(userId) }
                        launch { observeMonthData(userId) }
                        launch { observeTodaySpend(userId) }
                        launch { observeSelectedDateTransactions(userId) }
                        launch { observePreviousMonthBalance(userId) }
                    }
                }
        }
    }

    private suspend fun observeUser(userId: Int) {
        userRepository.getUser(userId).collectLatest { user ->
            _uiState.update { it.copy(user = user) }
        }
    }

    private suspend fun observeRecentTransactions(userId: Int) {
        transactionRepository.getRecent(userId).collectLatest { list ->
            _uiState.update { it.copy(recentTransactions = list) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeMonthData(userId: Int) {
        selectedMonthFlow.flatMapLatest { month ->
            val start = month.atDay(1).format(dateFmt)
            val end = month.atEndOfMonth().format(dateFmt)

            combine(
                transactionRepository.getTotal(userId, "credit", start, end),
                transactionRepository.getTotal(userId, "debit", start, end),
                transactionRepository.getDailyTotalsForMonth(userId, start, end),
                dailyLimitRepository.getHistoryForUser(userId)
            ) { income, expense, dailyTotals, limitHistory ->
                MonthData(month, income, expense, dailyTotals, limitHistory)
            }
        }.collectLatest { data ->
            val balance = data.income - data.expense
            val totalFlow = data.income + data.expense
            val incomePct = if (totalFlow > 0) ((data.income / totalFlow) * 100).toInt() else 0
            val expensePct = if (totalFlow > 0) ((data.expense / totalFlow) * 100).toInt() else 0

            val spentByDate = data.dailyTotals.associate { it.transactionDate to it.spent }

            val calendarDays = (1..data.month.lengthOfMonth()).map { day ->
                val date = data.month.atDay(day)
                val spent = spentByDate[date.format(dateFmt)] ?: 0.0
                val limitForThatDay = resolveDailyLimitForDate(data.limitHistory, date)
                CalendarDay(date = date, isOverLimit = limitForThatDay > 0 && spent > limitForThatDay)
            }

            _uiState.update {
                it.copy(
                    selectedMonth = data.month,
                    monthlyIncome = data.income,
                    monthlyExpense = data.expense,
                    monthlyBalance = balance,
                    incomePercent = incomePct,
                    expensePercent = expensePct,
                    calendarDays = calendarDays,
                    isLoading = false
                )
            }
        }
    }

    private suspend fun observePreviousMonthBalance(userId: Int) {
        selectedMonthFlow.flatMapLatest { month ->
            val prevMonth = month.minusMonths(1)
            val start = prevMonth.atDay(1).format(dateFmt)
            val end = prevMonth.atEndOfMonth().format(dateFmt)
            combine(
                transactionRepository.getTotal(userId, "credit", start, end),
                transactionRepository.getTotal(userId, "debit", start, end)
            ) { income, expense -> income - expense }
        }.collectLatest { prevBalance ->
            _uiState.update { it.copy(lastMonthBalance = prevBalance) }
        }
    }

    private data class MonthData(
        val month: YearMonth,
        val income: Double,
        val expense: Double,
        val dailyTotals: List<DailyTotal>,
        val limitHistory: List<DailyLimitEntity>
    )


    private suspend fun observeTodaySpend(userId: Int) {
        val today = LocalDate.now()
        val todayStr = today.format(dateFmt)

        combine(
            transactionRepository.getDailySpend(userId, todayStr),
            dailyLimitRepository.getHistoryForUser(userId)
        ) { spend, history -> spend to resolveDailyLimitForDate(history, today) }
            .collectLatest { (spend, limit) ->
                _uiState.update {
                    it.copy(
                        todaySpend = spend,
                        todayDailyLimit = limit,
                        isDailyLimitExceeded = limit > 0 && spend > limit
                    )
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeSelectedDateTransactions(userId: Int) {
        selectedDateFlow.flatMapLatest { date ->
            transactionRepository.getForDate(userId, date.format(dateFmt))
        }.collectLatest { list ->
            _uiState.update { it.copy(selectedDateTransactions = list) }
        }
    }

    fun onMonthChanged(newMonth: YearMonth) {
        selectedMonthFlow.value = newMonth
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        selectedDateFlow.value = date
    }
}