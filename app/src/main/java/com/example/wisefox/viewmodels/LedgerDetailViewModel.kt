package com.example.wisefox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LedgerDetailUiState {
    object Loading : LedgerDetailUiState()
    data class Success(val transactions: List<TransactionResponse>) : LedgerDetailUiState()
    data class Error(val message: String) : LedgerDetailUiState()
}

class LedgerDetailViewModel(
    private val ledgerId: Long,
    private val repo: LedgerRepository = LedgerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LedgerDetailUiState>(LedgerDetailUiState.Loading)
    val uiState: StateFlow<LedgerDetailUiState> = _uiState

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = LedgerDetailUiState.Loading
            try {
                val txs = repo.getTransactionsByLedger(ledgerId)
                _uiState.value = LedgerDetailUiState.Success(txs)
            } catch (e: Exception) {
                _uiState.value = LedgerDetailUiState.Error(e.message ?: "Error")
            }
        }
    }
}

class LedgerDetailViewModelFactory(private val ledgerId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LedgerDetailViewModel(ledgerId) as T
    }
}