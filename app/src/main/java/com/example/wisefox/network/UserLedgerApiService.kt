package com.example.wisefox.network

import com.example.wisefox.model.UserLedgerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserLedgerApiService {

    @GET("api/userledger/ledger/{ledgerId}/members")
    suspend fun getMembersByLedger(
        @Path("ledgerId") ledgerId: Long
    ): Response<List<UserLedgerResponse>>

    @POST("api/userledger/share-by-email")
    suspend fun shareByEmail(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Void>
}