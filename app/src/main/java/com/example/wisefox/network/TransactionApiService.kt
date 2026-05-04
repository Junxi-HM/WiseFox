package com.example.wisefox.network

import com.example.wisefox.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TransactionApiService {

    @GET("api/transactions/{ledgerId}")
    suspend fun getTransactions(
        @Path("ledgerId") ledgerId: Long
    ): Response<List<TransactionResponse>>

    @POST("api/transactions/create")
    suspend fun createTransaction(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<TransactionResponse>

    @DELETE("api/transactions/delete/{transactionId}")
    suspend fun deleteTransaction(
        @Path("transactionId") transactionId: Long
    ): Response<Void>
}