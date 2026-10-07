package com.example.pfsm.ui.theme.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.toTransactionReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider

import com.example.pfsm.viewModels.CalendarDayInfo
import com.example.pfsm.viewModels.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import com.example.pfsm.ui.theme.util.toSmartAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBack: () -> Unit = {}
) {
    val uiState by calendarViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .responsiveWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Spending Calendar",
                        fontSize = 20.sp,
                    )
                }
            }
            item {
                MonthNavigator(
                    monthLabel = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    currentMonth = uiState.selectedMonth,
                    onPrevious = { calendarViewModel.onMonthChanged(uiState.selectedMonth.minusMonths(1)) },
                    onNext = { calendarViewModel.onMonthChanged(uiState.selectedMonth.plusMonths(1)) },
                    onMonthSelected = { calendarViewModel.onMonthChanged(it) }
                )
            }

            item {
                if (uiState.todayDailyLimit <= 0) {
                    Text(
                        "Set a daily limit in Profile to see days highlighted here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LegendRow()
                }
            }

            item {
                CalendarGrid(
                    month = uiState.selectedMonth,
                    days = uiState.days,
                    selectedDate = uiState.selectedDate,
                    onDateClick = { calendarViewModel.onDateSelected(it) }
                )
            }

            item {
                SelectedDateHeader(
                    date = uiState.selectedDate,
                    spent = uiState.selectedDateSpent,
                    limit = uiState.selectedDateLimit
                )
            }

            if (uiState.selectedDateTransactions.isEmpty()) {
                item {
                    Text(
                        "No transactions on this date.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(uiState.selectedDateTransactions, key = { it.id }) { txn ->
                    CalendarTransactionRow(txn)
                }
            }
        }
    }


}

@Composable
private fun MonthNavigator(
    monthLabel: String,
    currentMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onMonthSelected: (YearMonth) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            monthLabel,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { showPicker = true }
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }

    if (showPicker) {
        MonthYearPickerDialog(
            initialMonth = currentMonth,
            onDismiss = { showPicker = false },
            onConfirm = {
                onMonthSelected(it)
                showPicker = false
            }
        )
    }
}

@Composable
private fun LegendRow() {
    val colors = FinanceColors
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendDot(color = colors.Expense, label = "Over limit")
        LegendDot(color = colors.Income, label = "Within limit")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    days: List<CalendarDayInfo>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val weekdayLabels = (0..6).map { offset ->
        DayOfWeek.of(
            ((DayOfWeek.MONDAY.value - 1 + offset) % 7) + 1
        ).getDisplayName(
            TextStyle.SHORT,
            LocalLocale.current.platformLocale
        )
    }

    val firstDayOfMonth = month.atDay(1).dayOfWeek

    val leadingBlanks =
        (firstDayOfMonth.value - DayOfWeek.MONDAY.value + 7) % 7

    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendar
        val allDays = buildList {
            repeat(leadingBlanks) {
                add(null)
            }
            days.forEach {
                add(it)
            }
        }
        allDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (day != null) {
                            CalendarDayCell(
                                day = day,
                                isSelected = day.date == selectedDate,
                                onClick = {
                                    onDateClick(day.date)
                                }
                            )
                        }
                    }
                }
                repeat(7 - week.size) {
                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = FinanceColors

    val backgroundColor = when {
        day.isOverLimit ->
            colors.Expense.copy(alpha = 0.18f)

        day.hasSpending ->
            colors.Income.copy(alpha = 0.12f)

        else -> Color.Transparent
    }

    val isToday = day.date == LocalDate.now()

    val todayBarColor = when {
        isToday -> colors.Gold
        else -> Color.Transparent
    }

    val textColor = when {
        day.isOverLimit -> colors.Expense
        day.hasSpending -> colors.Income
        else -> MaterialTheme.colorScheme.onSurface
    }
    val borderColor = when {
        isToday && isSelected -> colors.Gold
        isSelected -> textColor
        else -> Color.Transparent
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(2.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        }

        if(!isSelected && isToday){
            Spacer(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .width(5.dp)
                    .height(5.dp)
                    .background(todayBarColor)

            )
        }

    }
}


@Composable
private fun SelectedDateHeader(
    date: LocalDate,
    spent: Double,
    limit: Double
) {
    val colors = FinanceColors
    val dateText = when (date) {
        LocalDate.now() -> "Today"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))
    }
    val isOverLimit = limit > 0 && spent > limit

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = dateText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )


        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Spent: ₹${spent.toSmartAmount()}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isOverLimit) colors.Expense else colors.Income
            )
            if (limit > 0) {
                Text(
                    text = "Limit: ₹${limit.toSmartAmount()}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun CalendarTransactionRow(txn: TransactionEntity) {
    val colors = FinanceColors
    val isCredit = txn.type == "credit"
    val amountColor = if (isCredit) colors.Income else colors.Expense
    val sign = if (isCredit) "+" else "−"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = amountColor,
                    start = Offset(
                        x = 0f,
                        y = 14.dp.toPx()
                    ),
                    end = Offset(
                        x = 0f,
                        y = size.height - 14.dp.toPx()
                    ),
                    strokeWidth = 2.dp.toPx()
                )
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = txn.description?.trim()?.takeIf { it.isNotBlank() }
                    ?: (if (isCredit) "Income" else "Expense"),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Text(
                text = "$sign₹${txn.amount.toSmartAmount()}",
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}