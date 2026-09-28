package com.example.pfsm.viewModels



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.datautil.resolveDailyLimitForDate
import com.example.pfsm.data.entities.TransactionEntity


import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.session.SessionManager

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

data class CalendarDayInfo(
    val date: LocalDate,
    val spent: Double,
    val limitThatDay: Double,   // the limit that was ACTUALLY in effect on this date
    val isOverLimit: Boolean,   // red
    val hasSpending: Boolean    // any expense at all that day (used for a neutral/dot state)
)

data class CalendarUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val todayDailyLimit: Double = 0.0,
    val days: List<CalendarDayInfo> = emptyList(),

    val selectedDate: LocalDate = LocalDate.now(),
    val selectedDateTransactions: List<TransactionEntity> = emptyList(),

    val selectedDateLimit: Double = 0.0,
    val selectedDateSpent: Double = 0.0,

    val isLoading: Boolean = true
)
class CalendarViewModel(
    private val transactionRepository: TransactionRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val selectedMonthFlow = MutableStateFlow(YearMonth.now())
    private val selectedDateFlow = MutableStateFlow(LocalDate.now())

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    coroutineScope {
                        launch { observeMonthGrid(userId) }
                        launch { observeSelectedDateTransactions(userId) }
                    }
                }
        }
    }


    private suspend fun observeMonthGrid(userId: Int) {
        selectedMonthFlow.flatMapLatest { month ->
            val start = month.atDay(1).format(dateFmt)
            val end = month.atEndOfMonth().format(dateFmt)
            combine(
                transactionRepository.getDailyTotalsForMonth(userId, start, end),
                dailyLimitRepository.getHistoryForUser(userId)
            ) { dailyTotals, limitHistory -> Triple(month, dailyTotals, limitHistory) }
        }.collectLatest { (month, dailyTotals, limitHistory) ->
            val spentByDate = dailyTotals.associate { it.transactionDate to it.spent }

            val days = (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
                val spent = spentByDate[date.format(dateFmt)] ?: 0.0
                val limitForThatDay = resolveDailyLimitForDate(limitHistory, date)
                CalendarDayInfo(
                    date = date,
                    spent = spent,
                    limitThatDay = limitForThatDay,
                    isOverLimit = limitForThatDay > 0 && spent > limitForThatDay,
                    hasSpending = spent > 0
                )
            }

            val todayLimit = resolveDailyLimitForDate(limitHistory, LocalDate.now())

            _uiState.update { currentState ->
                val selectedInfo = days.find { it.date == currentState.selectedDate }
                currentState.copy(
                    selectedMonth = month,
                    days = days,
                    todayDailyLimit = todayLimit,
                    selectedDateLimit = selectedInfo?.limitThatDay ?: 0.0,
                    selectedDateSpent = selectedInfo?.spent ?: 0.0,
                    isLoading = false
                )
            }
        }
    }

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
        val selectedInfo = _uiState.value.days.find { it.date == date }
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedDateLimit = selectedInfo?.limitThatDay ?: 0.0,
                selectedDateSpent = selectedInfo?.spent ?: 0.0
            )
        }
        selectedDateFlow.value = date
    }
}