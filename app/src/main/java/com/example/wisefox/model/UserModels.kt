package com.example.wisefox.model

// ── User response from backend ─────────────────────────────────────────────────
data class UserResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val role: String,               // "USER" | "PREMIUM"
    val hasProfilePicture: Boolean
)
