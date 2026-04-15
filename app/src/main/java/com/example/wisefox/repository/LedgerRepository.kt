package com.example.wisefox.repository

import com.example.wisefox.model.LedgerRequest
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.TransactionResponse
import com.example.wisefox.network.LedgerApiService
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.TransactionApiService

class LedgerRepository {

    private val ledgerApi = RetrofitClient.instance.create(LedgerApiService::class.java)
    private val transactionApi = RetrofitClient.instance.create(TransactionApiService::class.java)

    // ── READ ──────────────────────────────────────────────────────────────────

    suspend fun getLedgersByUser(userId: Long): List<LedgerResponse> {
        val response = ledgerApi.getLedgersByUser(userId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load ledgers: ${response.code()}")
    }

    suspend fun getTransactionsByLedger(ledgerId: Long): List<TransactionResponse> {
        val response = transactionApi.getTransactionsByLedger(ledgerId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load transactions: ${response.code()}")
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    suspend fun createLedger(request: LedgerRequest): LedgerResponse {
        val response = ledgerApi.createLedger(request)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Failed to create ledger: ${response.code()}")
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    suspend fun updateLedger(id: Long, request: LedgerRequest): LedgerResponse {
        val response = ledgerApi.updateLedger(id, request)
        if (response.isSuccessful) return response.body()!!
        throw Exception("Failed to update ledger: ${response.code()}")
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    suspend fun deleteLedger(id: Long) {
        val response = ledgerApi.deleteLedger(id)
        if (!response.isSuccessful) throw Exception("Failed to delete ledger: ${response.code()}")
    }
}