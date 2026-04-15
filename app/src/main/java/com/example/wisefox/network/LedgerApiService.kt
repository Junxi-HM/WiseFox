package com.example.wisefox.network

import com.example.wisefox.model.LedgerRequest
import com.example.wisefox.model.LedgerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface LedgerApiService {

    @GET("api/ledgers/user/{userId}")
    suspend fun getLedgersByUser(@Path("userId") userId: Long): Response<List<LedgerResponse>>

    @POST("api/ledgers/create")
    suspend fun createLedger(@Body request: LedgerRequest): Response<LedgerResponse>

    @PUT("api/ledgers/{id}")
    suspend fun updateLedger(
        @Path("id") id: Long,
        @Body request: LedgerRequest
    ): Response<LedgerResponse>

    @DELETE("api/ledgers/{id}")
    suspend fun deleteLedger(@Path("id") id: Long): Response<Void>
}