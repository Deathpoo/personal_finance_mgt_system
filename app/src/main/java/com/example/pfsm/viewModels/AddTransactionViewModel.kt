package com.example.pfsm.viewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pfsm.data.datautil.resolveDailyLimitForDate
import com.example.pfsm.data.entities.CategoryEntity
import com.example.pfsm.data.entities.TransactionEntity

import com.example.pfsm.data.repository.CategoryRepository
import com.example.pfsm.data.repository.DailyLimitRepository
import com.example.pfsm.data.repository.TransactionRepository
import com.example.pfsm.data.session.SessionManager
import androidx.lifecycle.SavedStateHandle
import com.example.pfsm.ui.theme.util.toEditableAmountString

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddTransactionUiState(
    val currentUserId: Int? = null,

    val type: String = "debit",           // "debit" (expense) or "credit" (income)
    val amountText: String = "",
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val selectedCategoryId: Int? = null,

    val categories: List<CategoryEntity> = emptyList(),

    val amountError: String? = null,
    val showLimitWarning: Boolean = false,
    val pendingLimitWarningAmount: Double? = null,
    val limitForWarning: Double = 0.0,

    val isSaved: Boolean = false,

    val isEditMode: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isDeleted: Boolean = false,
)

class AddTransactionViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val dailyLimitRepository: DailyLimitRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val editTransactionId: Int? = savedStateHandle.get<String>("transactionId")?.toIntOrNull()
    private var originalTransaction: TransactionEntity? = null

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    init {
        viewModelScope.launch {
            sessionManager.currentUserId
                .filterNotNull()
                .collectLatest { userId ->
                    _uiState.update { it.copy(currentUserId = userId) }

                    if (editTransactionId != null) {
                        val existing = transactionRepository.getById(editTransactionId)
                        if (existing != null) {
                            originalTransaction = existing
                            _uiState.update {
                                it.copy(
                                    isEditMode = true,
                                    type = existing.type,
                                    amountText = existing.amount.toEditableAmountString(),
                                    description = existing.description ?: "",
                                    date = LocalDate.parse(existing.transactionDate),
                                    selectedCategoryId = existing.categoryId
                                )
                            }
                            loadCategories(userId, existing.type)
                            return@collectLatest
                        }
                    }
                    loadCategories(userId, _uiState.value.type)
                }
        }
    }


    private fun categoryTypeFor(transactionType: String): String =
        if (transactionType == "credit") "income" else "expense"

    private fun loadCategories(userId: Int, transactionType: String) {
        viewModelScope.launch {
            val categoryType = categoryTypeFor(transactionType)
            categoryRepository.getByType(userId, categoryType).collectLatest { categories ->
                _uiState.update {
                    val stillValid = categories.any { c -> c.id == it.selectedCategoryId }
                    it.copy(
                        categories = categories,
                        selectedCategoryId = if (stillValid) it.selectedCategoryId else null
                    )
                }
            }
        }
    }

    fun onTypeChanged(newType: String) {
        _uiState.update { it.copy(type = newType, selectedCategoryId = null) }
        val userId = _uiState.value.currentUserId ?: return
        loadCategories(userId, newType)
    }

    fun onAmountChanged(text: String) {
        val filtered = text.filterIndexed { index, c ->
            c.isDigit() || (c == '.' && text.indexOf('.') == index)
        }
        _uiState.update { it.copy(amountText = filtered, amountError = null) }
    }

    fun onDescriptionChanged(text: String) {
        _uiState.update { it.copy(description = text) }
    }

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onCategorySelected(categoryId: Int) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }


    fun attemptSave() {
        val state = _uiState.value
        val userId = state.currentUserId ?: return

        val amount = state.amountText.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(amountError = "Enter a valid amount") }
            return
        }

        if (state.type == "debit") {
            viewModelScope.launch {
                val history = dailyLimitRepository.getHistoryForUser(userId).first()
                val limitForDate = resolveDailyLimitForDate(history, state.date)

                if (limitForDate > 0) {
                    val dateStr = state.date.format(dateFmt)
                    var alreadySpent = transactionRepository.getDailySpendOnce(userId, dateStr)

                    // Editing: subtract the transaction's OWN original amount first,
                    // so we're not double-counting it against itself.
                    val original = originalTransaction
                    if (original != null && original.type == "debit" && original.transactionDate == dateStr) {
                        alreadySpent -= original.amount
                    }

                    val wouldExceed = (alreadySpent + amount) > limitForDate
                    if (wouldExceed) {
                        _uiState.update {
                            it.copy(
                                showLimitWarning = true,
                                pendingLimitWarningAmount = amount,
                                limitForWarning = limitForDate
                            )
                        }
                        return@launch
                    }
                }
                saveTransaction(userId, amount)
            }
        } else {
            saveTransaction(userId, amount)
        }
    }


    fun confirmSaveDespiteLimit() {
        val userId = _uiState.value.currentUserId ?: return
        val amount = _uiState.value.pendingLimitWarningAmount ?: return
        _uiState.update { it.copy(showLimitWarning = false, pendingLimitWarningAmount = null) }
        saveTransaction(userId, amount)
    }

    fun dismissLimitWarning() {
        _uiState.update { it.copy(showLimitWarning = false, pendingLimitWarningAmount = null) }
    }

    private fun saveTransaction(userId: Int, amount: Double) {
        val state = _uiState.value
        viewModelScope.launch {
            val original = originalTransaction
            if (original != null) {
                transactionRepository.update(
                    original.copy(
                        categoryId = state.selectedCategoryId,
                        type = state.type,
                        amount = amount,
                        description = state.description.ifBlank { null },
                        transactionDate = state.date.format(dateFmt)
                    )
                )
            } else {
                transactionRepository.add(
                    TransactionEntity(
                        userId = userId,
                        categoryId = state.selectedCategoryId,
                        type = state.type,
                        amount = amount,
                        description = state.description.ifBlank { null },
                        transactionDate = state.date.format(dateFmt)
                    )
                )
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDelete() {
        val txn = originalTransaction ?: return
        viewModelScope.launch {
            transactionRepository.delete(txn)
            _uiState.update { it.copy(showDeleteConfirm = false, isDeleted = true) }
        }
    }
}