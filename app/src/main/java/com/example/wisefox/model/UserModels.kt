package com.example.wisefox.model

// ── User response from backend ─────────────────────────────────────────────────
// FIX #4: added passwordHash so we can re-send the existing bcrypt hash when
//         updating the profile without changing the password.
//         BCrypt hashes are one-way and safe to transmit.
data class UserResponse(
    val id: Long,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val role: String,                 // "USER" | "PREMIUM"
    val hasProfilePicture: Boolean,
    val passwordHash: String? = null  // bcrypt hash from backend (nullable for safety)
)
