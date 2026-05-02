package com.example.wisefox.network

import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    // ── GET user by ID ─────────────────────────────────────────────────────
    @GET("api/user/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<UserResponse>

    // ── GET profile picture ────────────────────────────────────────────────
    @GET("api/user/{id}/pfp")
    suspend fun getProfilePicture(@Path("id") id: Long): Response<ResponseBody>

    // ── UPDATE user (multipart: fields + optional image) ──────────────────
    // FIX: password is RequestBody (non-null) — always sent as a multipart
    //      Part so Spring @RequestParam(required=true) never throws 500.
    //      The repository sends "" when no password change is requested;
    //      UserService.update() skips re-encoding when the value is blank.
    @Multipart
    @PUT("api/user/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Part("name")     name:     RequestBody,
        @Part("surname")  surname:  RequestBody,
        @Part("username") username: RequestBody,
        @Part("email")    email:    RequestBody,
        @Part("password") password: RequestBody,   // always sent; "" = no change
        @Part pfpFile: MultipartBody.Part?
    ): Response<UserResponse>

    // ── GET ledgers for user ───────────────────────────────────────────────
    @GET("api/ledgers/user/{userId}")
    suspend fun getLedgers(@Path("userId") userId: Long): Response<List<LedgerResponse>>

    // ── SHARE ledger by email ──────────────────────────────────────────────
    @POST("api/userledger/share-by-email")
    suspend fun shareLedgerByEmail(@Body request: ShareLedgerRequest): Response<Unit>
}

// ── Request model ──────────────────────────────────────────────────────────────
data class ShareLedgerRequest(
    val ownerUserId: Long,
    val ledgerId: Long,
    val targetEmail: String
)