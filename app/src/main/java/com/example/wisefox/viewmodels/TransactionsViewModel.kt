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
import java.time.LocalDate

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class TxCrudState {
    object Idle    : TxCrudState()
    object Loading : TxCrudState()
    object Success : TxCrudState()
    data class Error(val message: String) : TxCrudState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class TransactionsViewModel : ViewModel() {

    private val ledgerApi: LedgerApiService =
        RetrofitClient.instance.create(LedgerApiService::class.java)
    private val txApi: TransactionApiService =
        RetrofitClient.instance.create(TransactionApiService::class.java)

    private val _soloLedgers   = MutableStateFlow<List<LedgerResponse>>(emptyList())
    private val _sharedLedgers = MutableStateFlow<List<LedgerResponse>>(emptyList())
    val soloLedgers:   StateFlow<List<LedgerResponse>> = _soloLedgers
    val sharedLedgers: StateFlow<List<LedgerResponse>> = _sharedLedgers

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _crudState = MutableStateFlow<TxCrudState>(TxCrudState.Idle)
    val crudState: StateFlow<TxCrudState> = _crudState

    private var currentIsShared = false

    init { loadAll() }

    fun loadAll() {
        val userId = SessionManager.getUserId()
        if (userId < 0) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = ledgerApi.getLedgers(userId)
                if (!resp.isSuccessful) { _isLoading.value = false; return@launch }
                val all = resp.body() ?: emptyList()
                _soloLedgers.value   = all.filter { it.ownerId == userId }
                _sharedLedgers.value = all.filter { it.ownerId != userId }
                reloadTransactions()
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun setShared(isShared: Boolean) {
        currentIsShared = isShared
        viewModelScope.launch { reloadTransactions() }
    }

    private suspend fun reloadTransactions() {
        val ledgers = if (currentIsShared) _sharedLedgers.value else _soloLedgers.value

        // Lanzar todas las llamadas en paralelo dentro del scope de viewModelScope
        val deferreds: List<Deferred<List<TransactionResponse>>> = ledgers.map { ledger ->
            viewModelScope.async {
                try {
                    val r = txApi.getTransactions(ledger.id)
                    if (r.isSuccessful) r.body() ?: emptyList()
                    else emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        val all = deferreds.awaitAll()
            .flatten()
            .sortedByDescending { it.date }

        _transactions.value = all
        _isLoading.value = false
    }

    fun createTransaction(
        ledgerId: Long,
        amount: Double,
        type: String,
        category: String,
        note: String
    ) {
        _crudState.value = TxCrudState.Loading
        viewModelScope.launch {
            try {
                val body: Map<String, Any> = mapOf(
                    "ledgerId" to ledgerId,
                    "amount"   to amount,
                    "type"     to type,
                    "category" to category,
                    "date"     to LocalDate.now().toString(),
                    "note"     to note
                )
                val resp = txApi.createTransaction(body)
                if (resp.isSuccessful) {
                    _crudState.value = TxCrudState.Success
                    reloadTransactions()
                } else {
                    _crudState.value = TxCrudState.Error("Failed (${resp.code()})")
                }
            } catch (e: Exception) {
                _crudState.value = TxCrudState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetCrudState() { _crudState.value = TxCrudState.Idle }

    fun currentLedgers(): List<LedgerResponse> =
        if (currentIsShared) _sharedLedgers.value else _soloLedgers.value
}