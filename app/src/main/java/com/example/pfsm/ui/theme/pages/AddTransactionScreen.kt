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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.ui.theme.design.CategoryIcons
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.Atma
import com.example.pfsm.ui.theme.util.StackSansNotch
import com.example.pfsm.viewModels.AddTransactionViewModel
import com.example.pfsm.viewModels.AppViewModelProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FinanceColors

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
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
                Row(
                    modifier = Modifier.weight(2f),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Add Transaction",
                        fontSize = 20.sp,
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ){
                    if (uiState.isEditMode) {
                        IconButton(onClick = { viewModel.requestDelete() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete transaction")
                        }
                    }
                }
            }
            // Debit / Credit toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                TypeToggleButton(
                    label = "Expense",
                    selected = uiState.type == "debit",
                    color = colors.Expense,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTypeChanged("debit") }
                )
                TypeToggleButton(
                    label = "Income",
                    selected = uiState.type == "credit",
                    color = colors.Income,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTypeChanged("credit") }
                )
            }

            //  Amount
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::onAmountChanged,
                label = { Text("Amount") },
                leadingIcon = { Text("₹", fontSize = 18.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null,
                supportingText = uiState.amountError?.let { { Text(it, color = colors.Expense) } },
                modifier = Modifier.fillMaxWidth()
            )

            //  Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
            )

            //  Date
            Column {
                Text("Date", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(uiState.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                }
            }

            //  Category picker
            Column {
                Text("Category", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                if (uiState.categories.isEmpty()) {
                    Text(
                        "No categories yet for this type. Add one from Profile → Manage categories.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(uiState.categories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = category.id == uiState.selectedCategoryId,
                                onClick = { viewModel.onCategorySelected(category.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            //  Save
            Button(
                onClick = { viewModel.attemptSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Gold,
                    contentColor = colors.Ink
                )
            ) {
                Text(if (uiState.isEditMode) "Update transaction" else "Save transaction", fontWeight = FontWeight.SemiBold)
            }

            Column {
                QuoteCards("spend  with  purpose. save  with  confidence")
            }
        }
    }





    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onDateChanged(picked)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (uiState.showLimitWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLimitWarning,
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.Warning
                )
            },
            title = { Text("Over your daily limit") },
            text = {
                Text(
                    "This expense will take you over today's spending limit of " +
                            "₹${"%,.0f".format(uiState.limitForWarning)}. Add it anyway?"
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSaveDespiteLimit) {
                    Text("Add anyway", color = colors.Expense)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissLimitWarning) { Text("Cancel") }
            }
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("Delete transaction?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Delete", color = colors.Expense)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TypeToggleButton(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) color.copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CategoryChip(category: CategoryEntity, isSelected: Boolean, onClick: () -> Unit) {
    val colors = FinanceColors
    val tint = if (category.type == "income") colors.Income else colors.Expense

    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) tint.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                CategoryIcons.resolve(category.icon),
                contentDescription = category.name,
                tint = tint
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(category.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun QuoteCards(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "\"",
            fontFamily = StackSansNotch,
            fontSize = 100.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFD0D0D0),

            )

        Text(
            text = text,
            fontFamily = Atma,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 22.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.offset(y = (-60).dp)
        )

    }
}


@Preview(showBackground = true)
@Composable
fun QuoteCardsPreview() {
    PFSMTheme {
        QuoteCards("spend  with  purpose. save  with  confidence")
    }
}