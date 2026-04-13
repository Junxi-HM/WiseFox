package com.example.wisefox.repository

import com.example.wisefox.model.AuthResponse
import com.example.wisefox.model.GoogleAuthRequest
import com.example.wisefox.model.GoogleRegisterBody
import com.example.wisefox.model.LoginRequest
import com.example.wisefox.model.VerifyCodeBody
import com.example.wisefox.network.RetrofitClient
import com.example.wisefox.network.AuthApiService

class AuthRepository {

    private val api: AuthApiService =
        RetrofitClient.instance.create(AuthApiService::class.java)

    // ── 手写登录 ──────────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        }
        // 401 → 账号不存在或密码错误
        val code = response.code()
        throw Exception(
            when (code) {
                401  -> "EMAIL_NOT_FOUND"   // 前端用这个标记区分"建议用 Google 注册"
                else -> "Login failed ($code)"
            }
        )
    }

    // ── Google Step 1 ─────────────────────────────────────────────────────────
    suspend fun googleLogin(idToken: String): Map<String, String> {
        val response = api.googleLogin(GoogleAuthRequest(idToken))
        if (response.isSuccessful) return response.body() ?: emptyMap()
        throw Exception("Google login failed (${response.code()})")
    }

    // ── Google Step 2 ─────────────────────────────────────────────────────────
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

    // ── Google Step 3 ─────────────────────────────────────────────────────────
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