package com.example.wisefox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.network.LedgerApiService
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.TransactionApiService
import com.example.wisefox.utils.SessionManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI wrapper ────────────────────────────────────────────────────────────────

data class LedgerUiModel(
    val ledger: LedgerResponse,
    val totalExpense: Double = 0.0,
    val totalIncome: Double  = 0.0
)

// ── CRUD state ────────────────────────────────────────────────────────────────

sealed class LedgerCrudState {
    object Idle    : LedgerCrudState()
    object Loading : LedgerCrudState()
    object Success : LedgerCrudState()
    data class Error(val message: String) : LedgerCrudState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class HomeViewModel : ViewModel() {

    private val ledgerApi: LedgerApiService =
        RetrofitClient.instance.create(LedgerApiService::class.java)
    private val txApi: TransactionApiService =
        RetrofitClient.instance.create(TransactionApiService::class.java)

    private val _soloLedgers   = MutableStateFlow<List<LedgerUiModel>>(emptyList())
    private val _sharedLedgers = MutableStateFlow<List<LedgerUiModel>>(emptyList())
    val soloLedgers:   StateFlow<List<LedgerUiModel>> = _soloLedgers
    val sharedLedgers: StateFlow<List<LedgerUiModel>> = _sharedLedgers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _crudState = MutableStateFlow<LedgerCrudState>(LedgerCrudState.Idle)
    val crudState: StateFlow<LedgerCrudState> = _crudState

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadLedgers() {
        val userId = SessionManager.getUserId()
        if (userId < 0) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = ledgerApi.getLedgers(userId)
                if (!resp.isSuccessful) { _isLoading.value = false; return@launch }

                val all = resp.body() ?: emptyList()
                val solo   = all.filter { it.ownerId == userId }
                val shared = all.filter { it.ownerId != userId }

                // Load transactions in parallel for totals
                val soloUi:   List<LedgerUiModel> = solo.map { ledger ->
                    viewModelScope.async { loadUiModel(ledger) }
                }.awaitAll()

                val sharedUi: List<LedgerUiModel> = shared.map { ledger ->
                    viewModelScope.async { loadUiModel(ledger) }
                }.awaitAll()

                _soloLedgers.value   = soloUi
                _sharedLedgers.value = sharedUi
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    private suspend fun loadUiModel(ledger: LedgerResponse): LedgerUiModel {
        return try {
            val r = txApi.getTransactions(ledger.id)
            val txList: List<TransactionResponse> =
                if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
            LedgerUiModel(
                ledger       = ledger,
                totalExpense = txList.filter { it.type?.name == "EXPENSE" }.sumOf { it.amount ?: 0.0 },
                totalIncome  = txList.filter { it.type?.name == "INCOME"  }.sumOf { it.amount ?: 0.0 }
            )
        } catch (_: Exception) {
            LedgerUiModel(ledger = ledger)
        }
    }

    // ── Create ────────────────────────────────────────────────────────────────

    fun createLedger(name: String, currency: String, description: String) {
        val userId = SessionManager.getUserId()
        if (userId < 0) return

        _crudState.value = LedgerCrudState.Loading
        viewModelScope.launch {
            try {
                val body: Map<String, Any> = mapOf(
                    "name"        to name,
                    "currency"    to currency,
                    "description" to description,
                    "userId"      to userId
                )
                val resp = ledgerApi.createLedger(body)
                if (resp.isSuccessful) {
                    _crudState.value = LedgerCrudState.Success
                    loadLedgers()
                } else {
                    _crudState.value = LedgerCrudState.Error("Failed (${resp.code()})")
                }
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Error")
            }
        }
    }

    // ── Update ────────────────────────────────────────────────────────────────

    fun updateLedger(ledgerId: Long, name: String, currency: String, description: String) {
        _crudState.value = LedgerCrudState.Loading
        viewModelScope.launch {
            try {
                val body: Map<String, Any> = mapOf(
                    "name"        to name,
                    "currency"    to currency,
                    "description" to description,
                    "userId"      to SessionManager.getUserId()
                )
                val resp = ledgerApi.updateLedger(ledgerId, body)
                if (resp.isSuccessful) {
                    _crudState.value = LedgerCrudState.Success
                    loadLedgers()
                } else {
                    _crudState.value = LedgerCrudState.Error("Failed (${resp.code()})")
                }
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Error")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun deleteLedger(ledgerId: Long) {
        _crudState.value = LedgerCrudState.Loading
        viewModelScope.launch {
            try {
                val resp = ledgerApi.deleteLedger(ledgerId)
                if (resp.isSuccessful) {
                    _crudState.value = LedgerCrudState.Success
                    loadLedgers()
                } else {
                    _crudState.value = LedgerCrudState.Error("Failed (${resp.code()})")
                }
            } catch (e: Exception) {
                _crudState.value = LedgerCrudState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetCrudState() { _crudState.value = LedgerCrudState.Idle }
    fun findLedgerById(id: Long): LedgerResponse? {
        val all = _soloLedgers.value + _sharedLedgers.value
        return all.firstOrNull { it.ledger.id == id }?.ledger
    }
}