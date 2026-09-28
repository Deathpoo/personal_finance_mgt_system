package com.example.pfsm.ui.theme.pages


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.YearPickerDialog
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider

import com.example.pfsm.viewModels.BudgetCardUiModel
import com.example.pfsm.viewModels.BudgetViewModel
import java.time.format.DateTimeFormatter

@Composable
fun BudgetScreen(
    budgetViewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onAddBudget: () -> Unit = {},
    onBudgetClick: (Int) -> Unit = {}
) {
    val uiState by budgetViewModel.uiState.collectAsState()
    val colors = FinanceColors

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudget,
                containerColor = colors.Gold,
                contentColor = colors.Ink,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add budget")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxSize().responsiveWidth()) {

                // Monthly / Yearly toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    PeriodToggleButton(
                        label = "Monthly",
                        selected = uiState.periodType == "monthly",
                        modifier = Modifier.weight(1f),
                        onClick = { budgetViewModel.onPeriodTypeChanged("monthly") }
                    )
                    PeriodToggleButton(
                        label = "Yearly",
                        selected = uiState.periodType == "yearly",
                        modifier = Modifier.weight(1f),
                        onClick = { budgetViewModel.onPeriodTypeChanged("yearly") }
                    )
                }

                //  Month/Year navigator
                var showPicker by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        if (uiState.periodType == "monthly") {
                            budgetViewModel.onMonthChanged(uiState.selectedMonth.minusMonths(1))
                        } else {
                            budgetViewModel.onYearChanged(uiState.selectedYear - 1)
                        }
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
                    }
                    Text(
                        text = if (uiState.periodType == "monthly")
                            uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                        else
                            uiState.selectedYear.toString(),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { showPicker = true }
                    )
                    IconButton(onClick = {
                        if (uiState.periodType == "monthly") {
                            budgetViewModel.onMonthChanged(uiState.selectedMonth.plusMonths(1))
                        } else {
                            budgetViewModel.onYearChanged(uiState.selectedYear + 1)
                        }
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }

                if (showPicker) {
                    if (uiState.periodType == "monthly") {
                        MonthYearPickerDialog(
                            initialMonth = uiState.selectedMonth,
                            onDismiss = { showPicker = false },
                            onConfirm = {
                                budgetViewModel.onMonthChanged(it)
                                showPicker = false
                            }
                        )
                    } else {
                        YearPickerDialog(
                            initialYear = uiState.selectedYear,
                            onDismiss = { showPicker = false },
                            onConfirm = {
                                budgetViewModel.onYearChanged(it)
                                showPicker = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                //  Budget list
                if (uiState.budgetCards.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No budgets for this period. Tap + to create one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.budgetCards, key = { it.id }) { card ->
                            BudgetCard(card = card, onClick = { onBudgetClick(card.id) })
                        }
                    }
                }
            }
        }

        padding
    }
}

@Composable
private fun PeriodToggleButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FinanceColors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) colors.Gold.copy(alpha = 0.18f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) colors.Gold else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun BudgetCard(card: BudgetCardUiModel, onClick: () -> Unit) {
    val colors = FinanceColors
    val isOverBudget = card.percentUsed > 100
    val barColor = when {
        isOverBudget -> colors.Expense
        card.percentUsed > 80 -> colors.Warning
        else -> colors.Income
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSecondary),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(card.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        text = if (card.isOverall) "Overall budget" else card.categories.joinToString { it.name },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${card.percentUsed}%",
                    color = barColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (card.percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Spent ₹${card.spent.toReadableAmount()} of ₹${card.amount.toReadableAmount()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (card.remaining >= 0)
                        "₹${card.remaining.toReadableAmount()} left"
                    else
                        "₹${(-card.remaining).toReadableAmount()} over",
                    fontSize = 12.sp,
                    color = if (card.remaining >= 0) colors.Income else colors.Expense,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun BudgetCardPreview(){
    PFSMTheme {
        BudgetCard(
            BudgetCardUiModel(
                1,
                "Util Budget",
                50000.0,
                10000.0,
                40000.0,
                20,
                false,
                listOf(
                    CategoryEntity(
                        id = 1,
                        userId = 3,
                        name = "Food",
                        icon = "restaurant",
                        type = "debit"
                    )
                )
            ),
            onClick = {}
        )
    }
}