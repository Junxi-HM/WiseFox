package com.example.wisefox.repository

import com.example.wisefox.model.AuthResponse
import com.example.wisefox.model.GoogleAuthRequest
import com.example.wisefox.model.GoogleRegisterBody
import com.example.wisefox.model.LoginRequest
import com.example.wisefox.model.VerifyCodeBody
import com.example.wisefox.network.AuthApiService
import com.example.wisefox.network.RetrofitClient

class AuthRepository {

    private val api: AuthApiService =
        RetrofitClient.instance.create(AuthApiService::class.java)

    // ── Email + password login ─────────────────────────────────────────────────
    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        }
        val code = response.code()
        throw Exception(
            when (code) {
                401  -> "EMAIL_NOT_FOUND"
                else -> "Login failed ($code)"
            }
        )
    }

    // ── Google Step 1: send Google ID token ────────────────────────────────────
    // Returns map with keys: status, token, email, userId, name, surname, username, role
    suspend fun googleLogin(idToken: String): Map<String, String> {
        val response = api.googleLogin(GoogleAuthRequest(idToken))
        if (response.isSuccessful) return response.body() ?: emptyMap()
        throw Exception("Google login failed (${response.code()})")
    }

    // ── Google Step 2: verify 6-digit code ────────────────────────────────────
    suspend fun verifyCode(email: String, code: String): Map<String, String> {
        val response = api.verifyCode(VerifyCodeBody(email, code))
        if (response.isSuccessful) return response.body() ?: emptyMap()
        throw Exception(
            when (response.code()) {
                400  -> "Invalid or expired code"
                else -> "Verification failed (${response.code()})"
            }
        )
    }

    // ── Google Step 3: complete registration ───────────────────────────────────
    // Returns map with keys: token, userId, username, email, name, surname, role
    suspend fun registerWithGoogle(
        googleToken: String,
        username: String,
        name: String,
        surname: String,
        password: String
    ): Map<String, String> {
        val response = api.registerWithGoogle(
            GoogleRegisterBody(googleToken, username, name, surname, password)
        )
        if (response.isSuccessful) return response.body() ?: emptyMap()
        throw Exception(
            when (response.code()) {
                409  -> "Username or email already taken"
                else -> "Registration failed (${response.code()})"
            }
        )
    }
}