package com.example.pfsm.ui.theme.pages



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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.ui.theme.design.CategoryIcons
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.MAX_AMOUNT
import com.example.pfsm.ui.theme.util.MAX_TEXT_LENGTH
import com.example.pfsm.ui.theme.util.MonthYearPickerDialog
import com.example.pfsm.ui.theme.util.YearPickerDialog
import com.example.pfsm.ui.theme.util.amountExceedsMax
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.viewModels.AddBudgetViewModel
import com.example.pfsm.viewModels.AppViewModelProvider
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    viewModel: AddBudgetViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FinanceColors

    val amountTooLarge = amountExceedsMax(uiState.amountText)

    var showPeriodPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .responsiveWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Add Budget",
                    fontSize = 20.sp,
                )
            }
            //  Budget type
            Column {
                Text("Budget type", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ToggleButton(
                        label = "Overall",
                        selected = uiState.budgetType == "overall",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onBudgetTypeChanged("overall") }
                    )
                    ToggleButton(
                        label = "Category-wise",
                        selected = uiState.budgetType == "category",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onBudgetTypeChanged("category") }
                    )
                }
                Text(
                    text = if (uiState.budgetType == "overall")
                        "Tracks every expense, regardless of category."
                    else
                        "Tracks spending only in the categories you pick below.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text("Budget name") },
                singleLine = true,
                isError = uiState.nameError != null,
                supportingText = {
                    Text(
                        uiState.nameError ?: "${uiState.name.length}/$MAX_TEXT_LENGTH",
                        color = if (uiState.nameError != null) colors.Expense else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Amount ----------
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::onAmountChanged,
                label = { Text("Amount") },
                leadingIcon = { Text("₹", fontSize = 18.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null || amountTooLarge,
                supportingText = {
                    val message = uiState.amountError
                        ?: if (amountTooLarge) "Maximum amount is ₹${MAX_AMOUNT.toReadableAmount()}" else null
                    message?.let { Text(it, color = colors.Expense) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Period
            Column {
                Text("Period", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    ToggleButton(
                        label = "Monthly",
                        selected = uiState.periodType == "monthly",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onPeriodTypeChanged("monthly") }
                    )
                    ToggleButton(
                        label = "Yearly",
                        selected = uiState.periodType == "yearly",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.onPeriodTypeChanged("yearly") }
                    )
                }
            }

            // ---------- Which month / year ----------
            Column {
                Text(
                    if (uiState.periodType == "monthly") "Month" else "Year",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showPeriodPicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (uiState.periodType == "monthly")
                            uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                        else
                            uiState.selectedYear.toString()
                    )
                }
            }

            if (showPeriodPicker) {
                if (uiState.periodType == "monthly") {
                    MonthYearPickerDialog(
                        initialMonth = uiState.selectedMonth,
                        onDismiss = { showPeriodPicker = false },
                        onConfirm = {
                            viewModel.onMonthSelected(it)
                            showPeriodPicker = false
                        }
                    )
                } else {
                    YearPickerDialog(
                        initialYear = uiState.selectedYear,
                        onDismiss = { showPeriodPicker = false },
                        onConfirm = {
                            viewModel.onYearSelected(it)
                            showPeriodPicker = false
                        }
                    )
                }
            }

            //  Category picker (category-wise only)
            if (uiState.budgetType == "category") {
                Column {
                    Text(
                        "Select categories (${uiState.selectedCategoryIds.size} selected)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    if (uiState.expenseCategories.isEmpty()) {
                        Text(
                            "No expense categories yet. Add one from Profile → Manage categories.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.height(((uiState.expenseCategories.size + 2) / 3 * 76).dp)
                        ) {
                            items(uiState.expenseCategories, key = { it.id }) { category ->
                                CategorySelectTile(
                                    category = category,
                                    isSelected = category.id in uiState.selectedCategoryIds,
                                    onClick = { viewModel.onCategoryToggled(category.id) }
                                )
                            }
                        }
                    }
                    uiState.categoryError?.let {
                        Text(it, color = colors.Expense, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.save() },
                enabled = !amountTooLarge,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.Gold, contentColor = colors.Ink)
            ) {
                Text("Save budget", fontWeight = FontWeight.SemiBold)
            }

            Column {
                QuoteCards("every  rupee  counts")
            }
        }
    }
}

@Composable
private fun ToggleButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
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
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CategorySelectTile(category: CategoryEntity, isSelected: Boolean, onClick: () -> Unit) {
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