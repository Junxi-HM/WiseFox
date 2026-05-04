package com.example.wisefox.repository

import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.ShareLedgerRequest
import com.example.wisefox.network.UserApiService
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

class UserRepository {

    private val api: UserApiService =
        RetrofitClient.instance.create(UserApiService::class.java)

    private val imageJpeg = "image/jpeg".toMediaTypeOrNull()

    /**
     * Builds a text multipart Part with NO Content-Type and NO
     * Content-Transfer-Encoding header.
     *
     * Why this matters:
     *   - If we add `Content-Transfer-Encoding: binary` (OkHttp's default
     *     when using @Part("name") RequestBody), Spring's MultipartResolver
     *     rejects the whole request with "Failed to parse multipart
     *     servlet request".
     *   - If we add `Content-Type: text/plain`, Tomcat refuses to register
     *     the part in request.getParameterMap(), so @RequestParam reads it
     *     as missing.
     *   - With NEITHER header, Tomcat treats it as a plain form field,
     *     and @RequestParam picks it up correctly.
     *
     * The body itself does carry a media type (null = no Content-Type
     * header on the part), but we explicitly write only the
     * Content-Disposition header.
     */
    private fun textPart(fieldName: String, value: String): MultipartBody.Part {
        val body = value.toRequestBody(null)
        val headers = Headers.headersOf(
            "Content-Disposition", "form-data; name=\"$fieldName\""
        )
        return MultipartBody.Part.create(headers, body)
    }

    // ── Fetch user data ────────────────────────────────────────────────────────
    suspend fun getUser(id: Long): UserResponse {
        val response = api.getUserById(id)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty user body")
        }
        throw Exception("Failed to load user (${response.code()})")
    }

    // ── Fetch profile picture bytes ────────────────────────────────────────────
    suspend fun getProfilePicture(id: Long): ResponseBody? {
        val response = api.getProfilePicture(id)
        return if (response.isSuccessful) response.body() else null
    }

    // ── Update user profile ────────────────────────────────────────────────────
    suspend fun updateUser(
        id: Long,
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String?,
        pfpBytes: ByteArray?
    ): UserResponse {

        val passwordPart: MultipartBody.Part? = password
            ?.takeIf { it.isNotBlank() }
            ?.let { textPart("password", it) }

        // Image part DOES carry image/jpeg Content-Type (and a filename).
        // That part is read via MultipartFile, not via @RequestParam String,
        // so the same Tomcat rule does not block it.
        val pfpPart: MultipartBody.Part? = pfpBytes?.let { bytes ->
            MultipartBody.Part.createFormData(
                "pfpFile",
                "avatar.jpg",
                bytes.toRequestBody(imageJpeg)
            )
        }

        val response = api.updateUser(
            id       = id,
            name     = textPart("name",     name),
            surname  = textPart("surname",  surname),
            username = textPart("username", username),
            email    = textPart("email",    email),
            password = passwordPart,
            pfpFile  = pfpPart
        )

        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty update response body")
        }

        val errorMsg = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
            ?: "Update failed (${response.code()})"
        throw Exception(errorMsg)
    }

    // ── Share a ledger by email ────────────────────────────────────────────────
    suspend fun shareLedgerByEmail(
        ownerUserId: Long,
        ledgerId: Long,
        targetEmail: String
    ) {
        val response = api.shareLedgerByEmail(
            ShareLedgerRequest(ownerUserId, ledgerId, targetEmail)
        )
        if (!response.isSuccessful) {
            throw Exception("Share failed (${response.code()})")
        }
    }

    // ── List ledgers for a user ────────────────────────────────────────────────
    suspend fun getLedgers(userId: Long): List<LedgerResponse> {
        val response = api.getLedgers(userId)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }
        throw Exception("Failed to load ledgers (${response.code()})")
    }
}