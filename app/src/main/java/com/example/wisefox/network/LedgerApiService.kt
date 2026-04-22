package com.example.wisefox.network

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
    suspend fun getLedgers(
        @Path("userId") userId: Long
    ): Response<List<LedgerResponse>>

    @GET("api/ledgers/{id}")
    suspend fun getLedgerById(
        @Path("id") id: Long
    ): Response<LedgerResponse>

    @POST("api/ledgers/create")
    suspend fun createLedger(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<LedgerResponse>

    @PUT("api/ledgers/{id}")
    suspend fun updateLedger(
        @Path("id") id: Long,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<LedgerResponse>

    @DELETE("api/ledgers/{id}")
    suspend fun deleteLedger(
        @Path("id") id: Long
    ): Response<Void>
}