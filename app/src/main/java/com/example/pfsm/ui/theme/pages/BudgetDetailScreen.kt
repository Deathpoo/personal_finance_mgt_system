package com.example.pfsm.ui.theme.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import com.example.pfsm.ui.theme.design.CategoryIcons
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.util.MAX_AMOUNT
import com.example.pfsm.ui.theme.util.MAX_TEXT_LENGTH
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.YearPickerDialog
import com.example.pfsm.ui.theme.util.amountExceedsMax
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.ui.theme.util.toSmartAmount
import com.example.pfsm.ui.theme.util.toTransactionReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.BudgetDetailUiState
import com.example.pfsm.viewModels.BudgetDetailViewModel
import com.example.pfsm.viewModels.CategoryBreakdownItem
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailScreen(
    viewModel: BudgetDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.notFound) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "This budget no longer exists.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = uiState.name.ifBlank { "Budget" },
                        fontSize = 20.sp,
                    )
                }
                Row {
                    IconButton(onClick = { viewModel.startEdit() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit budget")
                    }
                    //---new change
                    IconButton(onClick = { viewModel.startDeleteBudget() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete budget")
                    }
                }
            }
        }

        item {
            SummaryCard(
                periodLabel = uiState.periodLabel,
                amount = uiState.amount,
                spent = uiState.spent,
                remaining = uiState.remaining,
                percentUsed = uiState.percentUsed,
                isOverall = uiState.isOverall
            )
        }

        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                Text("Category breakdown", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            items(uiState.categoryBreakdown, key = { "category_${it.categoryId}" }) { item ->
                CategoryBreakdownRow(
                    item = item,
                    isSelected = uiState.selectedCategoryId == item.categoryId,
                    onClick = { viewModel.onCategorySelected(item.categoryId) }
                )
            }
        }

        item {
            Text(
                text = if (uiState.selectedCategoryId != null)
                    "Transactions — filtered"
                else
                    "All transactions",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }

        if (uiState.visibleTransactions.isEmpty()) {
            item {
                Text(
                    "No transactions here yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            itemsIndexed(
                items = uiState.visibleTransactions,
                key = { _, txn -> "${uiState.selectedCategoryId}_txn_${txn.id}" }
            ) { index, txn ->
                AnimatedTransactionItem(
                    txn = txn,
                    index = index
                )
            }
        }
    }


    if (uiState.isEditing) {
        EditBudgetDialog(
            uiState = uiState,
            onNameChanged = viewModel::onEditNameChanged,
            onAmountChanged = viewModel::onEditAmountChanged,
            onMonthSelected = viewModel::onEditMonthSelected,
            onYearSelected = viewModel::onEditYearSelected,
            onCategoryToggled = viewModel::onEditCategoryToggled,
            onCancel = viewModel::cancelEdit,
            onSave = viewModel::saveEdit
        )
    }


    if (uiState.isDeleting) {
        DeleteBudgetDialog(
            budgetName = uiState.name,
            onDelete = {
                viewModel.confirmDeleteBudget()
                onBack()
            },
            onCancel = viewModel::cancelDeleteBudget
        )
    }
}


@Composable
private fun DeleteBudgetDialog(
    budgetName: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Delete \"${budgetName}\" ?") },
        text = { Text("Are you sure, want to delete.") },
        confirmButton = {
            TextButton(onClick = onDelete) { Text("Delete", color = FinanceColors.Expense) }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditBudgetDialog(
    uiState: BudgetDetailUiState,
    onNameChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onYearSelected: (Int) -> Unit,
    onCategoryToggled: (Int) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val colors = FinanceColors

    var showPicker by remember { mutableStateOf(false) }
    val amountTooLarge = amountExceedsMax(uiState.editAmountText)

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Edit budget") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = uiState.editName,
                    onValueChange = onNameChanged,
                    label = { Text("Budget name") },
                    singleLine = true,
                    isError = uiState.editNameError != null,
                    supportingText = {
                        Text(
                            uiState.editNameError ?: "${uiState.name.length}/$MAX_TEXT_LENGTH",
                            color = if (uiState.editNameError != null) colors.Expense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))


                OutlinedTextField(
                    value = uiState.editAmountText,
                    onValueChange = onAmountChanged,
                    label = { Text("Amount") },
                    leadingIcon = { Text("₹") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = uiState.editAmountError != null || amountTooLarge,
                    supportingText = {
                        val message = uiState.editAmountError
                            ?: if (amountTooLarge) "Maximum amount is ₹${MAX_AMOUNT.toReadableAmount()}" else null
                        message?.let { Text(it, color = colors.Expense) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showPicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (uiState.periodType == "monthly")
                            uiState.editMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                        else
                            uiState.editYear.toString()
                    )
                }

                if (!uiState.isOverall) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Categories (${uiState.editSelectedCategoryIds.size} selected)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    if (uiState.editableExpenseCategories.isEmpty()) {
                        Text(
                            "No expense categories available.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(((uiState.editableExpenseCategories.size + 2) / 3 * 76).dp)
                        ) {
                            items(uiState.editableExpenseCategories, key = { it.id }) { category ->
                                EditCategoryTile(
                                    category = category,
                                    isSelected = category.id in uiState.editSelectedCategoryIds,
                                    onClick = { onCategoryToggled(category.id) }
                                )
                            }
                        }
                    }
                    uiState.editCategoryError?.let {
                        Text(
                            it,
                            color = colors.Expense,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This is an overall budget — it tracks every expense regardless of category.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = !amountTooLarge
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )

    if (showPicker) {
        if (uiState.periodType == "monthly") {
            MonthYearPickerDialog(
                initialMonth = uiState.editMonth,
                onDismiss = { showPicker = false },
                onConfirm = { onMonthSelected(it); showPicker = false }
            )
        } else {
            YearPickerDialog(
                initialYear = uiState.editYear,
                onDismiss = { showPicker = false },
                onConfirm = { onYearSelected(it); showPicker = false }
            )
        }
    }
}

@Composable
private fun EditCategoryTile(category: CategoryEntity, isSelected: Boolean, onClick: () -> Unit) {
    val colors = FinanceColors
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.Expense.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            CategoryIcons.resolve(category.icon),
            contentDescription = null,
            tint = if (isSelected) colors.Expense else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(category.name, fontSize = 11.sp)
    }
}

@Composable
private fun SummaryCard(
    periodLabel: String,
    amount: Double,
    spent: Double,
    remaining: Double,
    percentUsed: Int,
    isOverall: Boolean
) {
    val colors = FinanceColors
    val barColor = when {
        percentUsed > 100 -> colors.Expense
        percentUsed > 80 -> colors.Warning
        else -> colors.Income
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.Ink)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOverall) "Overall · $periodLabel" else "Category-wise · $periodLabel",
                    color = colors.TextMutedOnInk,
                    fontSize = 12.sp
                )
                Text(
                    "$percentUsed%",
                    color = barColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "₹${spent.toReadableAmount()}",
                color = colors.Gold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "of ₹${amount.toReadableAmount()} budget",
                color = colors.TextMutedOnInk,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = colors.TextMutedOnInk.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (remaining >= 0)
                    "₹${"%,.0f".format(remaining)} remaining"
                else
                    "₹${"%,.0f".format(-remaining)} over budget",
                color = if (remaining >= 0) colors.Income else colors.Expense,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    item: CategoryBreakdownItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = FinanceColors

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "Card_Scale"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .scale(cardScale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        border = if (isSelected) BorderStroke(1.dp, colors.Expense) else null,

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
                    .background(colors.Expense.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    CategoryIcons.resolve(item.icon),
                    contentDescription = null,
                    tint = colors.Expense,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    "${item.percentOfSpent}% of spend",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "₹${item.spent.toReadableAmount()}",
                color = colors.Expense,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BudgetDetailTransactionRow(txn: TransactionEntity) {
    val colors = FinanceColors
    val formattedDate = try {
        LocalDate.parse(txn.transactionDate).format(DateTimeFormatter.ofPattern("dd MMM"))
    } catch (e: Exception) {
        txn.transactionDate
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = colors.Expense,
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
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.description?.trim()?.takeIf { it.isNotBlank() } ?: "Expense",
                    fontSize = 14.sp
                )
                Text(
                    formattedDate,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "−₹${txn.amount.toSmartAmount()}",
                color = colors.Expense,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AnimatedTransactionItem(
    txn: TransactionEntity,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }


    val delayMillis = index * 50

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(
                durationMillis = 250,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        BudgetDetailTransactionRow(txn = txn)
    }
}