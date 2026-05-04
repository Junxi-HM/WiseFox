package com.example.wisefox.model

import com.google.gson.annotations.SerializedName

// ── Login request ─────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

// ── Login / Register response ─────────────────────────────────────────────────
data class AuthResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val role: String?,      // "USER" | "PREMIUM"
    val token: String?,
    val message: String?
)

// ── Google Step 1 request ─────────────────────────────────────────────────────
data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String
)

// ── Google Step 2 request ─────────────────────────────────────────────────────
data class VerifyCodeBody(
    val email: String,
    val code: String
)

// ── Google Step 3 request ─────────────────────────────────────────────────────
data class GoogleRegisterBody(
    val googleToken: String,
    val username: String,
    val name: String,
    val surname: String,
    val password: String
)