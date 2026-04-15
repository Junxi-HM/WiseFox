package com.example.wisefox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.LedgerRequest
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.LedgerUiModel
import com.example.wisefox.repository.LedgerRepository
import com.example.wisefox.utils.SessionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI States ─────────────────────────────────────────────────────────────────

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val ledgers: List<LedgerUiModel>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

sealed class LedgerCrudState {
    object Idle : LedgerCrudState()
    object Loading : LedgerCrudState()
    object Success : LedgerCrudState()
    data class Error(val message: String) : LedgerCrudState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class HomeViewModel(
    private val repo: LedgerRepository = LedgerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    // Resultado de operaciones create/update/delete
    private val _crudState = MutableStateFlow<LedgerCrudState>(LedgerCrudState.Idle)
    val crudState: StateFlow<LedgerCrudState> = _crudState

    // true = Shared, false = Solo
    private val _isShared = MutableStateFlow(false)
    val isShared: StateFlow<Boolean> = _isShared

    private var allLedgerUiModels: List<LedgerUiModel> = emptyList()

    init {
        loadLedgers()
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    fun loadLedgers() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val userId = SessionManager.getUserId()
                val ledgers: List<LedgerResponse> = repo.getLedgersByUser(userId)

                val uiModels: List<LedgerUiModel> = ledgers
                    .map { ledger ->
                        async {
                            val transactions = try {
                                repo.getTransactionsByLedger(ledger.id)
                            } catch (e: Exception) {
                                emptyList()
                            }
                            val totalExpenses = transactions
                                .filter { it.type.uppercase() == "EXPENSE" }
                                .sumOf { it.amount ?: 0.0 }
                                .toFloat()
                            val totalEarnings = transactions
                                .filter { it.type.uppercase() == "INCOME" }
                                .sumOf { it.amount ?: 0.0 }
                                .toFloat()
                            LedgerUiModel(
                                id            = ledger.id,
                                name          = ledger.name,
                                currency      = ledger.currency,
                                description   = ledger.description,
                                ownerId       = ledger.ownerId,
                                ownerUsername = ledger.ownerUsername,
                                totalExpenses = totalExpenses,
                                totalEarnings = totalEarnings
                            )
                        }
                    }
                    .awaitAll()

                allLedgerUiModels = uiModels
                applyFilter()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    fun createLedger(name: String, currency: String, description: String?) {
        viewModelScope.launch {
            _crudState.value = LedgerCrudState.Loading
            try {
                val request = LedgerRequest(
                    name        = name.trim(),
                    currency    = currency.trim(),
                    description = description?.trim()?.ifBlank { null },
                    userId      = SessionManager.getUserId()
                )
                repo.createLedger(request)
                _crudState.value = LedgerCrudState.Success
                loadLedgers()
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Failed to create ledger")
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    fun updateLedger(id: Long, name: String, currency: String, description: String?) {
        viewModelScope.launch {
            _crudState.value = LedgerCrudState.Loading
            try {
                val request = LedgerRequest(
                    name        = name.trim(),
                    currency    = currency.trim(),
                    description = description?.trim()?.ifBlank { null },
                    userId      = SessionManager.getUserId()
                )
                repo.updateLedger(id, request)
                _crudState.value = LedgerCrudState.Success
                loadLedgers()
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Failed to update ledger")
            }
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    fun deleteLedger(id: Long) {
        viewModelScope.launch {
            _crudState.value = LedgerCrudState.Loading
            try {
                repo.deleteLedger(id)
                _crudState.value = LedgerCrudState.Success
                loadLedgers()
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Failed to delete ledger")
            }
        }
    }

    // ── Filter & helpers ──────────────────────────────────────────────────────

    fun setSharedFilter(shared: Boolean) {
        _isShared.value = shared
        applyFilter()
    }

    fun resetCrudState() {
        _crudState.value = LedgerCrudState.Idle
    }

    fun getTotalExpenses(ledgers: List<LedgerUiModel>): String {
        if (ledgers.isEmpty()) return "—"
        val total = ledgers.sumOf { it.totalExpenses.toDouble() }.toFloat()
        return if (total == 0f) "—" else "${total.toInt()}€"
    }

    fun getTotalEarnings(ledgers: List<LedgerUiModel>): String {
        if (ledgers.isEmpty()) return "—"
        val total = ledgers.sumOf { it.totalEarnings.toDouble() }.toFloat()
        return if (total == 0f) "—" else "${total.toInt()}€"
    }

    private fun applyFilter() {
        val currentUserId = SessionManager.getUserId()
        val filtered = if (_isShared.value) {
            allLedgerUiModels.filter { it.ownerId != currentUserId }
        } else {
            allLedgerUiModels.filter { it.ownerId == currentUserId }
        }
        _uiState.value = HomeUiState.Success(filtered)
    }
}