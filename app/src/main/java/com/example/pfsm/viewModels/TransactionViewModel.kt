package com.example.pfsm.viewModels



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity
import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** null type = "All" (no type filter applied). */
data class TransactionFilters(
    val searchQuery: String = "",
    val type: String? = null,          // null / "credit" / "debit"
    val categoryIds: Set<Int> = emptySet(),  // empty = all categories
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null
) {
    val isAnyFilterActive: Boolean
        get() = type != null || categoryIds.isNotEmpty() || minAmount != null ||
                maxAmount != null || dateFrom != null || dateTo != null
}

data class TransactionListUiState(
    val filters: TransactionFilters = TransactionFilters(),
    val categories: List<CategoryEntity> = emptyList(),
    val categoriesById: Map<Int, CategoryEntity> = emptyMap(),


    val groupedTransactions: List<Pair<String, List<TransactionEntity>>> = emptyList(),

    val isFilterSheetOpen: Boolean = false,
    val isLoading: Boolean = true
)

class TransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    private val filtersFlow = MutableStateFlow(TransactionFilters())

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .flatMapLatest { userId ->
                    combine(
                        transactionRepository.getAllForUser(userId),
                        categoryRepository.getAllForUser(userId),
                        filtersFlow
                    ) { allTransactions, categories, filters ->
                        Triple(allTransactions, categories, filters)
                    }
                }
                .collectLatest { (allTransactions, categories, filters) ->
                    val filtered = applyFilters(allTransactions, filters)
                    val grouped = filtered
                        .groupBy { it.transactionDate }
                        .toList()
                        .sortedByDescending { it.first }

                    _uiState.update {
                        it.copy(
                            filters = filters,
                            categories = categories,
                            categoriesById = categories.associateBy { c -> c.id },
                            groupedTransactions = grouped,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun applyFilters(
        all: List<TransactionEntity>,
        filters: TransactionFilters
    ): List<TransactionEntity> {
        return all.filter { txn ->
            (filters.type == null || txn.type == filters.type) &&
                    (filters.categoryIds.isEmpty() || txn.categoryId in filters.categoryIds) &&
                    (filters.minAmount == null || txn.amount >= filters.minAmount) &&
                    (filters.maxAmount == null || txn.amount <= filters.maxAmount) &&
                    (filters.dateFrom == null || txn.transactionDate >= filters.dateFrom.toString()) &&
                    (filters.dateTo == null || txn.transactionDate <= filters.dateTo.toString()) &&
                    (filters.searchQuery.isBlank() ||
                            txn.description?.contains(filters.searchQuery, ignoreCase = true) == true ||
                            txn.amount.toString().contains(filters.searchQuery, ignoreCase = true) )
        }
    }

    fun onSearchQueryChanged(query: String) {
        filtersFlow.update { it.copy(searchQuery = query) }
    }


    fun onTypeFilterChanged(typeValue: String?) {
        filtersFlow.update { it.copy(type = typeValue, categoryIds = emptySet()) }
    }


    fun onCategoryFilterToggled(categoryId: Int) {
        filtersFlow.update {
            val current = it.categoryIds
            val updated = if (categoryId in current) current - categoryId else current + categoryId
            it.copy(categoryIds = updated)
        }
    }

    fun onClearCategoryFilter() {
        filtersFlow.update { it.copy(categoryIds = emptySet()) }
    }

    fun onAmountRangeChanged(min: Double?, max: Double?) {
        filtersFlow.update { it.copy(minAmount = min, maxAmount = max) }
    }

    fun onDateRangeChanged(from: LocalDate?, to: LocalDate?) {
        filtersFlow.update { it.copy(dateFrom = from, dateTo = to) }
    }

    fun clearFilters() {
        filtersFlow.update { TransactionFilters(searchQuery = it.searchQuery) }
    }

    fun openFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = true) }
    }

    fun closeFilterSheet() {
        _uiState.update { it.copy(isFilterSheetOpen = false) }
    }
}
