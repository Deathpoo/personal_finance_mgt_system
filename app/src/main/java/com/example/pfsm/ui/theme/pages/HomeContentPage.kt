package com.example.pfsm.ui.theme.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.R
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.viewModels.HomeUiState
import com.example.pfsm.viewModels.HomeViewModel
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.toTransactionReadableAmount
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.viewModels.AppViewModelProvider
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


@Composable
fun HomeContentPage(
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSeeAllTransactions: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onAddTransaction: () -> Unit ={}

) {
    val uiState by homeViewModel.uiState.collectAsState()

    val colors = FinanceColors
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = colors.Gold,
                contentColor = colors.Ink,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add budget")
            }
        }
    ){ padding ->

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .responsiveWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    BalanceDisplayCard(
                        uiState = uiState,
                        onPreviousMonth = { homeViewModel.onMonthChanged(uiState.selectedMonth.minusMonths(1)) },
                        onNextMonth = { homeViewModel.onMonthChanged(uiState.selectedMonth.plusMonths(1)) },
                        onMonthSelected = { homeViewModel.onMonthChanged(it) }
                    )
                }
                item {
                    IncomeExpenseSplitCard(uiState = uiState)
                }
                item {
                    DailyLimitCard(uiState = uiState)
                }
                item {
                    RecentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        onSeeAll = onSeeAllTransactions
                    )
                }

                item { CalendarShortcutCard(onClick = onOpenCalendar) }

                item {
                    padding
                }
            }
        }

    }


}

@Composable
fun BalanceDisplayCard(
    uiState: HomeUiState,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onMonthSelected: (YearMonth) -> Unit = {}
) {
    val monthLabel = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))

    Column {
        var showMonthPicker by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = monthLabel,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable { showMonthPicker = true }
            )
            IconButton(onClick = onNextMonth) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next month")
            }
        }

        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialMonth = uiState.selectedMonth,
                onDismiss = { showMonthPicker = false },
                onConfirm = { picked ->
                    onMonthSelected(picked)
                    showMonthPicker = false
                }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ), shape = RoundedCornerShape(34.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp,
                focusedElevation = 8.dp,
                hoveredElevation = 8.dp,
                draggedElevation = 12.dp
            )
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.blue_modern_linktree_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .weight(1.2f),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.balance),
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "₹${uiState.monthlyBalance.toReadableAmount()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = FinanceColors.Gold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MonthComparisonTrend(
                                currentBalance = uiState.monthlyBalance,
                                lastMonthBalance = uiState.lastMonthBalance
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.income),
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "₹${uiState.monthlyIncome.toReadableAmount()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .width(1.dp)
                                .size(40.dp),
                            color = Color.White
                        )
                        Column(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.expenses),
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "₹${uiState.monthlyExpense.toReadableAmount()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthComparisonTrend(currentBalance: Double, lastMonthBalance: Double) {
    val colors = FinanceColors
    val hasPreviousData = lastMonthBalance != 0.0
    val isIncrease = currentBalance >= lastMonthBalance

    val percentChange = if (hasPreviousData) {
        val rawChange = ((currentBalance - lastMonthBalance) / kotlin.math.abs(lastMonthBalance)) * 100
        kotlin.math.abs(rawChange.roundToInt())
    } else null

    val trendColor = if (isIncrease) colors.Income else colors.Expense
    val trendIcon = if (isIncrease) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Column(horizontalAlignment = Alignment.End) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Last month", color = colors.TextOnInk, fontSize = 11.sp)
            Text(
                text = "₹${lastMonthBalance.toReadableAmount()}",
                color = colors.TextMutedOnInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasPreviousData) {
                Icon(trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
            }
            Text(
                text = percentChange?.let { "$it%" } ?: "N/A",
                color = if (hasPreviousData) trendColor else colors.TextMutedOnInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
fun IncomeExpenseSplitCard(uiState: HomeUiState) {
    val total = uiState.monthlyIncome + uiState.monthlyExpense
    val incomeFraction = if (total > 0) (uiState.monthlyIncome / total).toFloat() else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "This month",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(incomeFraction.coerceIn(0.02f, 0.98f))
                        .fillMaxSize()
                        .background(FinanceColors.Income)
                )
                Box(
                    modifier = Modifier
                        .weight((1f - incomeFraction).coerceIn(0.02f, 0.98f))
                        .fillMaxSize()
                        .background(FinanceColors.Expense)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier =
                    Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendDot(
                    color = FinanceColors.Income,
                    label = "Income  ${uiState.incomePercent}%"
                )
                LegendDot(
                    color = FinanceColors.Expense,
                    label = "Expense  ${uiState.expensePercent}%"
                )
            }
        }
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
        Text(label, fontSize = 13.sp, color = Color(0xFF4A4D57))
    }
}

@Composable
fun DailyLimitCard(uiState: HomeUiState) {
    if (uiState.todayDailyLimit <= 0) return

    val progress = (uiState.todaySpend / uiState.todayDailyLimit).toFloat().coerceIn(0f, 1.5f)
    val barColor = if (uiState.isDailyLimitExceeded) FinanceColors.Expense else FinanceColors.Income

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Today's limit",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (uiState.isDailyLimitExceeded) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = FinanceColors.Expense,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Over limit",
                            color = FinanceColors.Expense,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = Color(0xFFEDEBE6)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "₹${uiState.todaySpend.toTransactionReadableAmount()} of ₹${uiState.todayDailyLimit.toTransactionReadableAmount()}",
                fontSize = 13.sp,
            )
        }
    }
}


@Composable
fun RecentTransactionsSection(
    transactions: List<TransactionEntity>,
    onSeeAll: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent transactions", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = "See all",
                color = FinanceColors.Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
        Spacer(Modifier.height(10.dp))
        if (transactions.isEmpty()) {
            Text(
                "No transactions yet this month.",
                fontSize = 13.sp,
                color = Color(0xFF8B8E99),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                transactions.forEach { txn -> TransactionRow(txn) }
            }
        }
    }
}

@Composable
private fun TransactionRow(txn: TransactionEntity) {
    val isCredit = txn.type == "credit"
    val amountColor = if (isCredit) FinanceColors.Income else FinanceColors.Expense
    val sign = if (isCredit) "+" else "−"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp,amountColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(amountColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = txn.description?.trim()?.takeIf { it.isNotBlank() }
                    ?: (if (isCredit) "Income" else "Expense"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(txn.transactionDate, fontSize = 12.sp, color = Color(0xFF8B8E99))
            }
            Text(
                text = "$sign₹${txn.amount.toTransactionReadableAmount()}",
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


@Composable
fun CalendarShortcutCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FinanceColors.Gold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = FinanceColors.Gold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Spending calendar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Text(
                    "See which days went over your limit",
                    fontSize = 12.sp,
                    color = Color(0xFF8B8E99)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB6B9C2))
        }
    }
}




@Preview(showBackground = true)
@Composable
fun BalanceDisplayCardPreview() {
    PFSMTheme {
        BalanceDisplayCard(
            uiState = HomeUiState(
                monthlyIncome = 100.0,
                monthlyExpense = 60.0,
                monthlyBalance = 40.0
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MonthlyEarnSpendCardPreview() {
    PFSMTheme {
        IncomeExpenseSplitCard(
            uiState = HomeUiState(
                monthlyIncome = 100.0,
                monthlyExpense = 60.0,
                monthlyBalance = -50.0,
                incomePercent = 20,
                expensePercent = 60
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecentTransPreview(){
    PFSMTheme {
        RecentTransactionsSection(
            listOf(
                TransactionEntity(
                    1,
                    2,
                    3,
                    "debit",
                    100.0,
                    "for food",
                    "2026-12-06"
                ),
                TransactionEntity(
                    1,
                    2,
                    3,
                    "credit",
                    100.0,
                    "salary",
                    "2026-12-06"
                )
            ),
            onSeeAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarShortcutCardPreview(){
    PFSMTheme {
        CalendarShortcutCard(onClick = {})
    }
}