package com.example.wisefox.network

import com.example.wisefox.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TransactionApiService {

    // 获取某账本的所有交易
    @GET("api/transactions/{ledgerId}")
    suspend fun getTransactionsByLedger(@Path("ledgerId") ledgerId: Long): Response<List<TransactionResponse>>
}