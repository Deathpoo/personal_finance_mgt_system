package com.example.pfsm.ui.theme.util



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pfsm.ui.theme.design.FinanceColors
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

private val YEAR_RANGE = 1990..(java.time.LocalDate.now().year + 10)


@Composable
fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialMonth.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to month") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ){
                YearScrollList(
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it }
                )
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                MonthGrid(
                    selectedMonth = if (selectedYear == initialMonth.year) initialMonth.month else null,
                    onMonthSelected = { month ->
                        onConfirm(YearMonth.of(selectedYear, month))
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


@Composable
fun YearPickerDialog(
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to year") },
        text = {
            YearScrollList(
                selectedYear = initialYear,
                onYearSelected = { onConfirm(it) }
            )
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun YearScrollList(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    val colors = FinanceColors
    val years = YEAR_RANGE.toList()
    val initialIndex = years.indexOf(selectedYear).coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem((initialIndex - 2).coerceAtLeast(0))
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.height(160.dp)
    ) {
        items(years) { year ->
            val isSelected = year == selectedYear
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.Gold.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onYearSelected(year) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    year.toString(),
                    color = if (isSelected) colors.Gold else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isSelected) 17.sp else 15.sp
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(selectedMonth: Month?, onMonthSelected: (Month) -> Unit) {
    val colors = FinanceColors
    val months = Month.entries

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(140.dp)
    ) {
        items(months) { month ->
            val isSelected = month == selectedMonth
            val label = month.getDisplayName(TextStyle.SHORT, LocalLocale.current.platformLocale)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) colors.Gold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onMonthSelected(month) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) colors.Gold else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}