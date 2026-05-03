package com.example.wisefox.network

import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import okhttp3.MultipartBody
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

    // ── UPDATE user (multipart) ────────────────────────────────────────────
    //
    // Cada part se construye en el repositorio con `MultipartBody.Part.create(headers, body)`
    // para garantizar que NO se envíe `Content-Transfer-Encoding: binary`
    // (que rompe el parser de Spring) PERO SÍ se envíe `Content-Type: text/plain`
    // (que Spring necesita para resolver `@RequestParam String`).
    //
    // `password` y `pfpFile` son opcionales: si se pasa null, el part se omite.
    @Multipart
    @PUT("api/user/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Part name:     MultipartBody.Part,
        @Part surname:  MultipartBody.Part,
        @Part username: MultipartBody.Part,
        @Part email:    MultipartBody.Part,
        @Part password: MultipartBody.Part?,
        @Part pfpFile:  MultipartBody.Part?
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