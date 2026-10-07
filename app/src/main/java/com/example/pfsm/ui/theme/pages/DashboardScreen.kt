package com.example.pfsm.ui.theme.pages


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.YearPickerDialog
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.ui.theme.util.toSmartAmount
import com.example.pfsm.ui.theme.util.toTransactionReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.CategorySpendItem
import com.example.pfsm.viewModels.DashboardViewModel
import com.example.pfsm.viewModels.DayBarData
import com.example.pfsm.viewModels.MonthBarData
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.math.roundToInt

// Fixed palette for donut segments
private val DonutPalette = listOf(
    Color(0xFFFB7185), Color(0xFFE8B34E), Color(0xFF34D399),
    Color(0xFF60A5FA), Color(0xFFA78BFA), Color(0xFFF472B6)
)

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val colors = FinanceColors

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().responsiveWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                YearSummaryCard(
                    year = uiState.selectedYear,
                    income = uiState.yearlyIncome,
                    expense = uiState.yearlyExpense,
                    balance = uiState.yearlyBalance,
                    savingsPercent = uiState.yearlySavingsPercent,
                    onPreviousYear = { viewModel.onYearChanged(uiState.selectedYear - 1) },
                    onNextYear = { viewModel.onYearChanged(uiState.selectedYear + 1) },
                    onYearLabelClick = { showYearPicker = true }
                )
            }



            item {
                Text("Income vs expense trend", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LegendDot(color = colors.Income, label = "Income")
                    LegendDot(color = colors.Expense, label = "Expense")
                }
                Spacer(Modifier.height(10.dp))
                TrendLineChart(data = uiState.monthlyData)
            }

            item {
                Text("Monthly spending", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                MonthlyBarChart(
                    data = uiState.monthlyData,
                    selectedMonth = uiState.selectedMonth.monthValue,
                    onBarClick = { viewModel.onMonthBarClicked(it) }
                )
            }


            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = { viewModel.onMonthSelected(uiState.selectedMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                    }
                    Text(
                        text = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showMonthPicker = true }
                    )
                    IconButton(onClick = { viewModel.onMonthSelected(uiState.selectedMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                    }
                }
                Text(
                    "Total spent: ₹${uiState.monthTotalSpend.toReadableAmount()} · tap a bar for that day's transactions",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                DailyBarChart(
                    data = uiState.dailyBreakdown,
                    selectedDay = uiState.selectedDay,
                    onDayClick = { viewModel.onDayBarClicked(it) }
                )
            }

            if (uiState.selectedDay != null) {
                item {
                    Text(
                        "Transactions — ${
                            uiState.selectedMonth.atDay(uiState.selectedDay!!)
                                .format(DateTimeFormatter.ofPattern("dd MMM"))
                        }",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                if (uiState.selectedDayTransactions.isEmpty()) {
                    item {
                        Text(
                            "No transactions this day.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(uiState.selectedDayTransactions, key = { "day_txn_${it.id}" }) { txn ->
                        DayTransactionRow(txn)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(
                        label = "Avg / day this month",
                        value = "₹${uiState.averageDailySpend.toTransactionReadableAmount()}",
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "Highest spend day",
                        value = uiState.highestSpendingDay?.let { "₹${it.amount.toTransactionReadableAmount()} (${it.day})" }
                            ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.hasLimitDataForMonth) {
                item {
                    LimitAdherenceCard(
                        withinCount = uiState.daysWithinLimit,
                        overCount = uiState.daysOverLimit
                    )
                }
            }

            if (uiState.topCategories.isNotEmpty()) {
                item {
                    Text("Top categories this year", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    CategoryDonutChart(categories = uiState.topCategories)
                }
            }
        }
    }



    if (showYearPicker) {
        YearPickerDialog(
            initialYear = uiState.selectedYear,
            onDismiss = { showYearPicker = false },
            onConfirm = {
                viewModel.onYearChanged(it)
                showYearPicker = false
            }
        )
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialMonth = uiState.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                viewModel.onMonthSelected(it)
                showMonthPicker = false
            }
        )
    }
}

@Composable
private fun YearSummaryCard(
    year: Int,
    income: Double,
    expense: Double,
    balance: Double,
    savingsPercent: Int,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onYearLabelClick: () -> Unit
) {
    val colors = FinanceColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.Ink)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onPreviousYear) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous year",
                        tint = colors.TextMutedOnInk
                    )
                }
                Text(
                    year.toString(),
                    color = colors.TextOnInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onYearLabelClick() }
                )
                IconButton(onClick = onNextYear) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next year",
                        tint = colors.TextMutedOnInk
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Income", color = colors.TextMutedOnInk, fontSize = 12.sp)
                    Text(
                        "₹${income.toReadableAmount()}",
                        color = colors.Income,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", color = colors.TextMutedOnInk, fontSize = 12.sp)
                    Text(
                        "₹${expense.toReadableAmount()}",
                        color = colors.Expense,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Saved", color = colors.TextMutedOnInk, fontSize = 12.sp)
                    Text(
                        "$savingsPercent%",
                        color = if (balance >= 0) colors.Income else colors.Expense,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(
    data: List<MonthBarData>,
    selectedMonth: Int,
    onBarClick: (Int) -> Unit
) {
    val colors = FinanceColors
    val maxAmount = data.maxOfOrNull { it.expense } ?: 0.0
    val maxBarHeight = 120.dp

    // 1. State variable to trigger the growth animation
    var isAnimated by remember { mutableStateOf(false) }

    // 2. Animate progress from 0f to 1f on launch/data load
    val animationProgress by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "BarChartGrowth"
    )
    // 3. Trigger the animation when the composable enters composition
    LaunchedEffect(Unit) {
        isAnimated = true
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxBarHeight + 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            val isSelected = bar.month == selectedMonth

            val heightFraction = if (maxAmount > 0) (bar.expense / maxAmount).toFloat() else 0f

            // Multiply target height by the animated progress (0 -> 1)
            val animatedFraction = heightFraction * animationProgress
            val barHeight = (maxBarHeight.value * animatedFraction).dp.coerceAtLeast(3.dp)


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onBarClick(bar.month) }
            ) {
                Box(
                    modifier = Modifier
                        .height(maxBarHeight)
                        .fillMaxWidth(0.6f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .height(barHeight)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (isSelected) Color(0xFF4A92DA) else colors.Expense.copy(alpha = 0.55f))
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    bar.label,
                    fontSize = 10.sp,
                    color = if (isSelected)  Color(0xFF4A92DA) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}



@Composable
private fun TrendLineChart(data: List<MonthBarData>) {
    val colors = FinanceColors
    if (data.isEmpty()) return
    val maxValue = data.maxOf { maxOf(it.income, it.expense) }.coerceAtLeast(1.0)

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )


    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            selectedIndex?.let { index ->
                val monthData = data.getOrNull(index)
                if (monthData != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "M${index + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Inc: ₹${monthData.income.toReadableAmount()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.Income
                            )
                            Text(
                                text = "Exp: ₹${monthData.expense.toReadableAmount()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.Expense
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .pointerInput(data) {
                    val stepX = size.width / (data.size - 1).coerceAtLeast(1)

                    awaitEachGesture {
                        // Detect initial touch down
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val initialIndex = (down.position.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                        selectedIndex = initialIndex

                        // Track pointer movement while holding down
                        do {
                            val event = awaitPointerEvent()
                            val currentPointer = event.changes.firstOrNull()
                            if (currentPointer != null && currentPointer.pressed) {
                                val index = (currentPointer.position.x / stepX).roundToInt().coerceIn(0, data.size - 1)
                                selectedIndex = index
                            }
                        } while (event.changes.any { it.pressed })

                        // When finger is lifted (all touch pointers released), reset state to hide popup
                        selectedIndex = null
                    }
                }
        ) {
            val labelSpaceHeight = 16.dp.toPx()
            val chartHeight = size.height - labelSpaceHeight
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)

            fun pointsFor(selector: (MonthBarData) -> Double): List<Offset> =
                data.mapIndexed { index, bar ->
                    val x = index * stepX
                    val y = chartHeight - (selector(bar) / maxValue * chartHeight).toFloat()
                    Offset(x, y)
                }

            val incomePoints = pointsFor { it.income }
            val expensePoints = pointsFor { it.expense }

            fun drawSeries(points: List<Offset>, color: Color) {
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = color,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
                points.forEach { drawCircle(color = color, radius = 4f, center = it) }
            }

            // Draw line series
            drawSeries(incomePoints, colors.Income)
            drawSeries(expensePoints, colors.Expense)

            // Draw guideline and highlight circles ONLY while actively pressing/dragging
            selectedIndex?.let { index ->
                val selectedX = index * stepX

                drawLine(
                    color = colors.TextMutedOnInk,
                    start = Offset(selectedX, 0f),
                    end = Offset(selectedX, chartHeight),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                incomePoints.getOrNull(index)?.let { point ->
                    drawCircle(color = colors.Income, radius = 7f, center = point)
                }
                expensePoints.getOrNull(index)?.let { point ->
                    drawCircle(color = colors.Expense, radius = 7f, center = point)
                }
            }

            // Draw month numbers below each point
            data.forEachIndexed { index, _ ->
                val x = index * stepX
                val labelText = "${index + 1}"
                val textLayoutResult = textMeasurer.measure(labelText, labelStyle)

                val textX = x - (textLayoutResult.size.width / 2f)
                val textY = chartHeight + 4.dp.toPx()

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textX, textY)
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
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DailyBarChart(data: List<DayBarData>, selectedDay: Int?, onDayClick: (Int) -> Unit) {
    val colors = FinanceColors
    val maxAmount = data.maxOfOrNull { it.amount } ?: 0.0
    val maxBarHeight = 90.dp


    val animationProgress = remember { Animatable(0f) }


    LaunchedEffect(data) {
        animationProgress.snapTo(0f) // Instantly reset bars to height 0
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .height(maxBarHeight + 20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        data.forEach { bar ->
            val isSelected = bar.day == selectedDay
            val heightFraction = if (maxAmount > 0) (bar.amount / maxAmount).toFloat() else 0f
            val animatedFraction = heightFraction * animationProgress.value
            val barHeight = (maxBarHeight.value * animatedFraction).dp.coerceAtLeast(2.dp)


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(20.dp)
                    .clickable { onDayClick(bar.day) }
            ) {
                Box(
                    modifier = Modifier.height(maxBarHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .height(barHeight)
                            .width(10.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(
                                when {
                                    isSelected ->  Color(0xFF4A92DA)
                                    bar.amount > 0 -> colors.Expense.copy(alpha = 0.7f)
                                    else -> colors.Expense.copy(alpha = 0.15f)
                                }
                            )
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    bar.day.toString(),
                    fontSize = 8.sp,
                    color = if (isSelected)  Color(0xFF4A92DA) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DayTransactionRow(txn: TransactionEntity) {
    val colors = FinanceColors
    val isCredit = txn.type == "credit"
    val amountColor = if (isCredit) colors.Income else colors.Expense
    val sign = if (isCredit) "+" else "−"

    Card(
        modifier = Modifier.fillMaxWidth()
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isCredit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = amountColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                txn.description?.trim()?.takeIf { it.isNotBlank() }
                    ?: (if (isCredit) "Income" else "Expense"),
                modifier = Modifier.weight(1f),
                fontSize = 13.sp
            )
            Text(
                "$sign₹${txn.amount.toSmartAmount()}",
                color = amountColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LimitAdherenceCard(withinCount: Int, overCount: Int) {
    val colors = FinanceColors
    val total = (withinCount + overCount).coerceAtLeast(1)
    val withinFraction = withinCount.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily limit adherence", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(withinFraction.coerceIn(0.02f, 0.98f))
                        .fillMaxSize()
                        .background(colors.Income)
                )
                Box(
                    modifier = Modifier
                        .weight((1f - withinFraction).coerceIn(0.02f, 0.98f))
                        .fillMaxSize()
                        .background(colors.Expense)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "$withinCount of ${withinCount + overCount} days within limit",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun CategoryDonutChart(categories: List<CategorySpendItem>) {
    val total = categories.sumOf { it.amount }.coerceAtLeast(0.01)


    val animationProgress = remember { Animatable(0f) }


    LaunchedEffect(categories) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 50f
                    var startAngle = -90f
                    val diameter = min(size.width, size.height) - stroke
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Total degrees to render based on current animation frame (0 to 360)
                    val currentTotalSweep = 360f * animationProgress.value

                    categories.forEachIndexed { index, item ->
                        val targetSweep = (item.amount / total * 360f).toFloat()

                        // Calculate how much of this specific slice should be rendered
                        val actualSweep = (currentTotalSweep - (startAngle - (-90f)))
                            .coerceIn(0f, targetSweep)

                        if (actualSweep > 0f) {
                            drawArc(
                                color = DonutPalette[index % DonutPalette.size],
                                startAngle = startAngle,
                                sweepAngle = actualSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = stroke, cap = StrokeCap.Butt)
                            )
                        }

                        startAngle += targetSweep
                    }
                }
                Text(
                    "${categories.size}\ncats",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                categories.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(DonutPalette[index % DonutPalette.size])
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(item.name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            "${item.percentOfExpense}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryDonutChartPreview(){
    PFSMTheme {
        CategoryDonutChart(
            listOf(
                CategorySpendItem(
                    1,
                    "Food",
                    "Icons.Default.Fastfood",
                    20000.0,
                    20
                ),
                CategorySpendItem(
                    2,
                    "Travel",
                    "Icons.Default.Fastfood",
                     40000.0,
                    50
                ),
                CategorySpendItem(
                    1,
                    "Education",
                    "Icons.Default.Fastfood",
                    30000.0,
                    30
                ),
                CategorySpendItem(
                    1,
                    "Food",
                    "Icons.Default.Fastfood",
                    20000.0,
                    20
                ),
                CategorySpendItem(
                    2,
                    "Travel",
                    "Icons.Default.Fastfood",
                    40000.0,
                    50
                ),
                CategorySpendItem(
                    1,
                    "Education",
                    "Icons.Default.Fastfood",
                    30000.0,
                    30
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun YearCardPreview(){
    PFSMTheme {
        YearSummaryCard(
            2026,
            7298.0,
            3769.0,
            2434.0,
            23,
            {},
            {},
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DailyCardPreview(){
    PFSMTheme {
        LimitAdherenceCard(
            5,
            10
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DailyChartPreview(){
    PFSMTheme {
        DailyBarChart(
            data = listOf(
                DayBarData(
                    1,
                    1000.0
                ),
                DayBarData(
                    2,
                    400.0
                ),
                DayBarData(
                    3,
                    1200.0
                ),
                DayBarData(
                    4,
                    800.0
                ),
                DayBarData(
                    5,
                    1000.0
                )
            ),
            5,
            {}
        )
    }
}