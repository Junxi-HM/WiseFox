package com.example.wisefox.network

import com.example.wisefox.model.AuthResponse
import com.example.wisefox.model.GoogleAuthRequest
import com.example.wisefox.model.GoogleRegisterBody
import com.example.wisefox.model.LoginRequest
import com.example.wisefox.model.VerifyCodeBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // ── 手写 email + password 登录 ─────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // ── Google Step 1: 发送 Google ID Token ────────────────────────────────
    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<Map<String, String>>

    // ── Google Step 2: 验证 6 位 code ──────────────────────────────────────
    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeBody): Response<Map<String, String>>

    // ── Google Step 3: 提交注册信息 ────────────────────────────────────────
    @POST("api/auth/register/google")
    suspend fun registerWithGoogle(@Body request: GoogleRegisterBody): Response<Map<String, String>>
}