package com.example.wisefox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.network.LedgerApiService
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.TransactionApiService
import com.example.wisefox.network.UserApiService
import com.example.wisefox.network.UserLedgerApiService
import com.example.wisefox.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LedgerDetailViewModel(private val ledgerId: Long) : ViewModel() {

    private val txApi: TransactionApiService =
        RetrofitClient.instance.create(TransactionApiService::class.java)
    private val ledgerApi: LedgerApiService =
        RetrofitClient.instance.create(LedgerApiService::class.java)
    private val userLedgerApi: UserLedgerApiService =
        RetrofitClient.instance.create(UserLedgerApiService::class.java)
    private val userApi: UserApiService =
        RetrofitClient.instance.create(UserApiService::class.java)

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _crudState = MutableStateFlow<TxCrudState>(TxCrudState.Idle)
    val crudState: StateFlow<TxCrudState> = _crudState

    init { loadTransactions() }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resp = txApi.getTransactions(ledgerId)
                _transactions.value = if (resp.isSuccessful)
                    (resp.body() ?: emptyList()).sortedByDescending { it.date }
                else
                    emptyList()
            } catch (_: Exception) {
                _transactions.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun createTransaction(
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
                    loadTransactions()
                } else {
                    _crudState.value = TxCrudState.Error("Failed (${resp.code()})")
                }
            } catch (e: Exception) {
                _crudState.value = TxCrudState.Error(e.message ?: "Error")
            }
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            try {
                txApi.deleteTransaction(txId)
                loadTransactions()
            } catch (_: Exception) { /* silent */ }
        }
    }

    // ── Update ledger ─────────────────────────────────────────────────────────

    fun updateLedger(name: String, currency: String, description: String) {
        viewModelScope.launch {
            try {
                val body: Map<String, Any> = mapOf(
                    "name"        to name,
                    "currency"    to currency,
                    "description" to description,
                    "userId"      to SessionManager.getUserId()
                )
                ledgerApi.updateLedger(ledgerId, body)
            } catch (_: Exception) { /* silent */ }
        }
    }

// ── Delete ledger ─────────────────────────────────────────────────────────

    fun deleteLedger() {
        viewModelScope.launch {
            try {
                ledgerApi.deleteLedger(ledgerId)
            } catch (_: Exception) { /* silent */ }
        }
    }

// ── Share ledger ──────────────────────────────────────────────────────────

    fun shareLedger(targetUsername: String, onResult: (error: String?) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. 用 username 查 email
                val userResp = userApi.getUserByUsername(targetUsername)
                if (!userResp.isSuccessful) {
                    onResult("User \"$targetUsername\" not found.")
                    return@launch
                }
                val email = userResp.body()?.email
                if (email.isNullOrBlank()) {
                    onResult("User \"$targetUsername\" not found.")
                    return@launch
                }

                // 2. 用已有 shareByEmail 接口
                val body: Map<String, Any> = mapOf(
                    "ownerUserId" to SessionManager.getUserId(),
                    "ledgerId"    to ledgerId,
                    "targetEmail" to email
                )
                val resp = userLedgerApi.shareByEmail(body)
                if (resp.isSuccessful) {
                    onResult(null)
                } else {
                    val errorJson = resp.errorBody()?.string() ?: ""
                    val message = when {
                        resp.code() == 409 -> "\"$targetUsername\" is already a member of this ledger."
                        resp.code() == 404 -> "User \"$targetUsername\" not found."
                        errorJson.contains("already a member", ignoreCase = true) ->
                            "\"$targetUsername\" is already a member of this ledger."
                        errorJson.contains("not found", ignoreCase = true) ->
                            "User \"$targetUsername\" not found."
                        else -> "Share failed (${resp.code()})"
                    }
                    onResult(message)
                }
            } catch (e: Exception) {
                onResult(e.message ?: "Share failed")
            }
        }
    }
    fun resetCrudState() { _crudState.value = TxCrudState.Idle }

    // ── Factory ───────────────────────────────────────────────────────────────
    class Factory(private val ledgerId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LedgerDetailViewModel(ledgerId) as T
    }
}