package com.example.wisefox.repository

import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.ShareLedgerRequest
import com.example.wisefox.network.UserApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class UserRepository {

    private val api: UserApiService =
        RetrofitClient.instance.create(UserApiService::class.java)

    // ── Fetch user data ────────────────────────────────────────────────────────
    suspend fun getUser(id: Long): UserResponse {
        val response = api.getUserById(id)
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty user body")
        throw Exception("Failed to load user (${response.code()})")
    }

    // ── Fetch profile picture bytes ────────────────────────────────────────────
    suspend fun getProfilePicture(id: Long): ResponseBody? {
        val response = api.getProfilePicture(id)
        return if (response.isSuccessful) response.body() else null
    }

    // ── Update user profile ────────────────────────────────────────────────────
    // FIX: always send the "password" part so Spring @RequestParam(required=true)
    //      never rejects the request. Send empty string when not changing password
    //      — the backend (UserService) skips encoding when the string is blank.
    suspend fun updateUser(
        id: Long,
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String?,       // null or blank = "do not change"
        pfpBytes: ByteArray?
    ): UserResponse {
        val toPlainBody = { s: String ->
            s.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        // Always send password field; empty string when no change is requested.
        // Backend UserService checks isBlank() and skips re-encoding if so.
        val passwordValue = password?.takeIf { it.isNotBlank() } ?: ""

        val pfpPart = pfpBytes?.let {
            MultipartBody.Part.createFormData(
                "pfpFile", "avatar.jpg",
                it.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
        }

        val response = api.updateUser(
            id       = id,
            name     = toPlainBody(name),
            surname  = toPlainBody(surname),
            username = toPlainBody(username),
            email    = toPlainBody(email),
            password = toPlainBody(passwordValue),   // always present, never null
            pfpFile  = pfpPart
        )
        if (response.isSuccessful) return response.body() ?: throw Exception("Empty update body")
        throw Exception("Update failed (${response.code()})")
    }

    // ── Fetch ledgers ──────────────────────────────────────────────────────────
    suspend fun getLedgers(userId: Long): List<LedgerResponse> {
        val response = api.getLedgers(userId)
        if (response.isSuccessful) return response.body() ?: emptyList()
        throw Exception("Failed to load ledgers (${response.code()})")
    }

    // ── Share ledger by email ──────────────────────────────────────────────────
    suspend fun shareLedgerByEmail(ownerUserId: Long, ledgerId: Long, targetEmail: String) {
        val response = api.shareLedgerByEmail(
            ShareLedgerRequest(
                ownerUserId = ownerUserId,
                ledgerId    = ledgerId,
                targetEmail = targetEmail
            )
        )
        if (!response.isSuccessful) {
            throw Exception("Share failed (${response.code()})")
        }
    }
}