package com.example.wisefox.network

import com.example.wisefox.model.UserLedgerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserLedgerApiService {

    @GET("api/userledger/ledger/{ledgerId}/members")
    suspend fun getMembersByLedger(
        @Path("ledgerId") ledgerId: Long
    ): Response<List<UserLedgerResponse>>
}