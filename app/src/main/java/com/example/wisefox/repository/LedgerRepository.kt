package com.example.wisefox.repository

import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.network.LedgerApiService
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.TransactionApiService

class LedgerRepository {

    private val ledgerApi: LedgerApiService =
        RetrofitClient.instance.create(LedgerApiService::class.java)

    private val txApi: TransactionApiService =
        RetrofitClient.instance.create(TransactionApiService::class.java)

    // ── Ledgers ───────────────────────────────────────────────────────────────

    suspend fun getLedgersByUser(userId: Long): List<LedgerResponse> {
        val resp = ledgerApi.getLedgers(userId)
        return if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
    }

    suspend fun getLedgerById(ledgerId: Long): LedgerResponse? {
        val resp = ledgerApi.getLedgerById(ledgerId)
        return if (resp.isSuccessful) resp.body() else null
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    suspend fun getTransactionsByLedger(ledgerId: Long): List<TransactionResponse> {
        val resp = txApi.getTransactions(ledgerId)
        return if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
    }

    suspend fun createTransaction(body: Map<String, Any>): TransactionResponse? {
        val resp = txApi.createTransaction(body)
        return if (resp.isSuccessful) resp.body() else null
    }

    suspend fun deleteTransaction(txId: Long) {
        txApi.deleteTransaction(txId)
    }
}