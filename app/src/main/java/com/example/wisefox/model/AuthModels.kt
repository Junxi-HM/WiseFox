package com.example.wisefox.model

import com.google.gson.annotations.SerializedName

// ── 登录请求 ──────────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

// ── 登录/注册响应 ──────────────────────────────────────────────────────────────
data class AuthResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val token: String?,
    val message: String?
)

// ── Google Step 1 请求 ────────────────────────────────────────────────────────
data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String
)

// ── Google Step 2 请求 ────────────────────────────────────────────────────────
data class VerifyCodeBody(
    val email: String,
    val code: String
)

// ── Google Step 3 请求 ────────────────────────────────────────────────────────
data class GoogleRegisterBody(
    val googleToken: String,
    val username: String,
    val name: String,
    val surname: String,
    val password: String
)