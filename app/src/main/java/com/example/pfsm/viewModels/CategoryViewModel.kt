package com.example.pfsm.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


enum class CategoryTab { INCOME, EXPENSE }

data class CategoryUiState(
    val currentUserId: Int? = null,
    val selectedTab: CategoryTab = CategoryTab.EXPENSE,
    val incomeCategories: List<CategoryEntity> = emptyList(),
    val expenseCategories: List<CategoryEntity> = emptyList()
) {
    val currentList: List<CategoryEntity>
        get() = if (selectedTab == CategoryTab.INCOME) incomeCategories else expenseCategories
}

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    _uiState.update { it.copy(currentUserId = userId) }
                    combine(
                        categoryRepository.getByType(userId, "income"),
                        categoryRepository.getByType(userId, "expense")
                    ) { income, expense -> income to expense }
                        .collectLatest { (income, expense) ->
                            _uiState.update {
                                it.copy(incomeCategories = income, expenseCategories = expense)
                            }
                        }
                }
        }
    }

    fun onTabSelected(tab: CategoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun addCategory(name: String, icon: String, type: String): String? {
        val userId = _uiState.value.currentUserId ?: return "Not signed in"
        val cleanName = name.trim()
        if (cleanName.isBlank()) return "Enter a category name"

        val sameTypeCategories =
            if (type == "income") _uiState.value.incomeCategories else _uiState.value.expenseCategories

        val duplicate = sameTypeCategories.firstOrNull { it.name.trim().equals(cleanName, ignoreCase = true) }
        if (duplicate != null) {
            val typeLabel = if (type == "income") "Income" else "Expense"
            return "\"${duplicate.name}\" already exists in $typeLabel"
        }

        viewModelScope.launch {
            categoryRepository.add(
                CategoryEntity(userId = userId, name = cleanName, icon = icon, type = type)
            )
        }
        return null
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { categoryRepository.delete(category) }
    }
}