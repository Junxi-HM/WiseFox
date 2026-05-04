package com.example.wisefox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.TransactionApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LedgerDetailViewModel(private val ledgerId: Long) : ViewModel() {

    private val txApi: TransactionApiService =
        RetrofitClient.instance.create(TransactionApiService::class.java)

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

    fun resetCrudState() { _crudState.value = TxCrudState.Idle }

    // ── Factory ───────────────────────────────────────────────────────────────
    class Factory(private val ledgerId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LedgerDetailViewModel(ledgerId) as T
    }
}