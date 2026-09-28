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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.ui.theme.design.CategoryIcons
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.toTransactionReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.TransactionFilters
import com.example.pfsm.viewModels.TransactionViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onTransactionClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FinanceColors

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.fillMaxSize().responsiveWidth())
        {
            // Search bar
            OutlinedTextField(
                value = uiState.filters.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search description") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    Box {
                        IconButton(onClick = { viewModel.openFilterSheet() }) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (uiState.filters.isAnyFilterActive) colors.Gold
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(30.dp)
            )

            // Type filter (All / Income / Expense)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TypeFilterChip(
                    label = "All",
                    selected = uiState.filters.type == null,
                    color = colors.Gold,
                    onClick = { viewModel.onTypeFilterChanged(null) }
                )
                TypeFilterChip(
                    label = "Income",
                    selected = uiState.filters.type == "credit",
                    color = colors.Income,
                    onClick = { viewModel.onTypeFilterChanged("credit") }
                )
                TypeFilterChip(
                    label = "Expense",
                    selected = uiState.filters.type == "debit",
                    color = colors.Expense,
                    onClick = { viewModel.onTypeFilterChanged("debit") }
                )
            }

            if (uiState.filters.isAnyFilterActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Filters applied",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Clear all",
                        fontSize = 12.sp,
                        color = colors.Expense,
                        modifier = Modifier.clickable { viewModel.clearFilters() }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Transaction list, grouped by date
            if (uiState.groupedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No transactions match your search.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.groupedTransactions.forEach { (date, transactions) ->
                        item(key = "header_$date") {
                            DateHeader(date = date)
                        }
                        items(transactions, key = { it.id }) { txn ->
                            TransactionListRow(
                                txn = txn,
                                category = uiState.categoriesById[txn.categoryId],
                                onClick = { onTransactionClick(txn.id) }
                            )
                        }
                    }
                }
            }
        }
    }



    if (uiState.isFilterSheetOpen) {
        FilterDialog(
            filters = uiState.filters,
            categories = uiState.categories,
            onDismiss = { viewModel.closeFilterSheet() },
            onCategoryToggled = viewModel::onCategoryFilterToggled,
            onClearCategoryFilter = viewModel::onClearCategoryFilter,
            onAmountRangeChanged = viewModel::onAmountRangeChanged,
            onDateRangeChanged = viewModel::onDateRangeChanged,
            onClear = viewModel::clearFilters
        )
    }
}

@Composable
private fun TypeFilterChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DateHeader(date: String) {

    val dateText = try {
        val parsedDate = LocalDate.parse(date)
        val today = LocalDate.now()

        when (parsedDate) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            today.minusDays(1) -> "Yesterday"
            else -> parsedDate.format(
                DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")
            )
        }

    } catch (e: DateTimeParseException) {
        date
    }

    Text(
        text = dateText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}


@Composable
private fun TransactionListRow(
    txn: TransactionEntity,
    category: CategoryEntity?,
    onClick: () -> Unit
) {
    val colors = FinanceColors
    val isCredit = txn.type == "credit"
    val amountColor = if (isCredit) colors.Income else colors.Expense
    val sign = if (isCredit) "+" else "−"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
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
                    imageVector = category?.let {
                        CategoryIcons.resolve(it.icon)
                    } ?: if (isCredit) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = null,
                    tint = amountColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = txn.description?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: category?.name
                        ?: if (isCredit) "Income" else "Expense",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = category?.name
                        ?: if (isCredit) "Income" else "Expense",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDialog(
    filters: TransactionFilters,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onCategoryToggled: (Int) -> Unit,
    onClearCategoryFilter: () -> Unit,
    onAmountRangeChanged: (Double?, Double?) -> Unit,
    onDateRangeChanged: (LocalDate?, LocalDate?) -> Unit,
    onClear: () -> Unit
) {
    val colors = FinanceColors

    var minText by remember { mutableStateOf(filters.minAmount?.toString() ?: "") }
    var maxText by remember { mutableStateOf(filters.maxAmount?.toString() ?: "") }
    var dateFrom by remember { mutableStateOf(filters.dateFrom) }
    var dateTo by remember { mutableStateOf(filters.dateTo) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }


    val relevantCategories = when (filters.type) {
        "credit" -> categories.filter { it.type == "income" }
        "debit" -> categories.filter { it.type == "expense" }
        else -> categories
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter transactions") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ){
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Category",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (filters.categoryIds.isNotEmpty()) {
                        Text(
                            "(${filters.categoryIds.size} selected)",
                            fontSize = 12.sp,
                            color = colors.Gold
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TypeFilterChip(
                            label = "All",
                            selected = filters.categoryIds.isEmpty(),
                            color = colors.Gold,
                            onClick = { onClearCategoryFilter() }
                        )
                    }
                    items(relevantCategories) { category ->
                        TypeFilterChip(
                            label = category.name,
                            selected = category.id in filters.categoryIds,
                            color = if (category.type == "income") colors.Income else colors.Expense,
                            onClick = { onCategoryToggled(category.id) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Amount range",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = {
                            minText = it
                            onAmountRangeChanged(it.toDoubleOrNull(), maxText.toDoubleOrNull())
                        },
                        label = { Text("Min") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = {
                            maxText = it
                            onAmountRangeChanged(minText.toDoubleOrNull(), it.toDoubleOrNull())
                        },
                        label = { Text("Max") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Date range",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateFilterField(
                        label = dateFrom?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "From",
                        modifier = Modifier.weight(1f),
                        onClick = { showFromPicker = true }
                    )
                    DateFilterField(
                        label = dateTo?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "To",
                        modifier = Modifier.weight(1f),
                        onClick = { showToPicker = true }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = {
                minText = ""; maxText = ""; dateFrom = null; dateTo = null
                onClear()
            }) { Text("Clear all", color = colors.Expense) }
        }
    )

    if (showFromPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dateFrom =
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateRangeChanged(dateFrom, dateTo)
                    }
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showToPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        dateTo =
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateRangeChanged(dateFrom, dateTo)
                    }
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun DateFilterField(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionRowPreview() {
    PFSMTheme {
        TransactionListRow(
            TransactionEntity(
                1,
                1,
                1,
                "credit",
                500000.0,
                """
                    
                    Gift
                    """,
                "09-09-2026",
                1788944885322
            ),
            CategoryEntity(
                1,
                1,
                "Gift",
                "gift",
                "income"
            ),
            {}
        )
    }
}