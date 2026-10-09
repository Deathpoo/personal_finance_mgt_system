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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.ui.theme.design.CategoryIcons
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.MAX_TEXT_LENGTH
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.CategoryTab
import com.example.pfsm.viewModels.CategoryViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryViewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onBack: () -> Unit = {}
) {
    val uiState by categoryViewModel.uiState.collectAsState()
    val colors = FinanceColors

    var showAddSheet by remember { mutableStateOf(false) }
    var categoryPendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    val tabIndex = if (uiState.selectedTab == CategoryTab.EXPENSE) 0 else 1

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                shape = RoundedCornerShape(50.dp),
                containerColor = colors.Gold,
                contentColor = colors.Ink
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxSize().responsiveWidth()) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Manage Categories",
                        fontSize = 20.sp,
                    )
                }

                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { categoryViewModel.onTabSelected(CategoryTab.EXPENSE) },
                        text = { Text("Expense") }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { categoryViewModel.onTabSelected(CategoryTab.INCOME) },
                        text = { Text("Income") }
                    )
                }

                val list = uiState.currentList

                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No categories yet. Tap + to add one.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(list, key = { it.id }) { category ->
                            CategoryRow(
                                category = category,
                                onDelete = { categoryPendingDelete = category }
                            )
                        }
                    }
                }
            }
        }

        padding
    }

    if (showAddSheet) {
        AddCategoryDialog(
            defaultType = if (uiState.selectedTab == CategoryTab.EXPENSE) "expense" else "income",
            onDismiss = { showAddSheet = false },
            onAdd = { name, icon, type ->
                val error = categoryViewModel.addCategory(name, icon, type)
                if (error == null) showAddSheet = false
                error
            }
        )
    }

    categoryPendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryPendingDelete = null },
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("Transactions using this category will keep their data, but lose the category link.") },
            confirmButton = {
                TextButton(onClick = {
                    categoryViewModel.deleteCategory(category)
                    categoryPendingDelete = null
                }) { Text("Delete", color = FinanceColors.Expense) }
            },
            dismissButton = {
                TextButton(onClick = { categoryPendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryRow(category: CategoryEntity, onDelete: () -> Unit) {
    val colors = FinanceColors
    val tint = if (category.type == "income") colors.Income else colors.Expense

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(CategoryIcons.resolve(category.icon), contentDescription = null, tint = tint)
            }
            Spacer(Modifier.width(12.dp))
            Text(category.name, modifier = Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.Expense)
            }
        }
    }
}

//  ADD CATEGORY DIALOG

@Composable
private fun AddCategoryDialog(
    defaultType: String,
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, type: String) -> String?
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(defaultType) }
    var selectedIcon by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val iconOptions = if (selectedType == "income") CategoryIcons.incomeKeys else CategoryIcons.expenseKeys
    val colors = FinanceColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add category") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip(
                        label = "Expense",
                        selected = selectedType == "expense",
                        color = colors.Expense,
                        onClick = { selectedType = "expense"; selectedIcon = null; errorText = null }
                    )
                    TypeChip(
                        label = "Income",
                        selected = selectedType == "income",
                        color = colors.Income,
                        onClick = { selectedType = "income"; selectedIcon = null }
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(MAX_TEXT_LENGTH); errorText = null },
                    label = { Text("Category name") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = {
                        Text(
                            errorText ?: "${name.length}/$MAX_TEXT_LENGTH",
                            color = if (errorText != null) colors.Expense else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Text("Choose an icon", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(160.dp)
                ) {
                    items(iconOptions) { key ->
                        val icon = CategoryIcons.resolve(key)
                        val isSelected = selectedIcon == key
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) colors.Gold.copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = key, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val icon = selectedIcon ?: iconOptions.firstOrNull() ?: "category"
                    errorText = onAdd(name.trim(), icon, selectedType)
                }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TypeChip(label: String, selected: Boolean, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
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
            fontWeight = FontWeight.Medium
        )
    }
}


