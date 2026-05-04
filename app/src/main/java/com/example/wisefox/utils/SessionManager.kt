package com.example.wisefox.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists JWT token and user info with SharedPreferences.
 * Call SessionManager.init(context) in Application.onCreate().
 */
object SessionManager {

    private const val PREF_NAME    = "wisefox_session"
    private const val KEY_TOKEN    = "jwt_token"
    private const val KEY_USER_ID  = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL    = "email"
    private const val KEY_NAME     = "name"
    private const val KEY_SURNAME  = "surname"
    private const val KEY_ROLE     = "role"
    private const val KEY_LANGUAGE = "language"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ── Token ──────────────────────────────────────────────────────────────────
    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?       = prefs.getString(KEY_TOKEN, null)
    fun isLoggedIn(): Boolean     = getToken() != null

    // ── Core user info (saved at login) ────────────────────────────────────────
    fun saveUser(id: Long, username: String, email: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getUserId(): Long     = prefs.getLong(KEY_USER_ID, -1L)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getEmail(): String    = prefs.getString(KEY_EMAIL, "") ?: ""

    // ── Extended user info (saved after profile fetch) ─────────────────────────
    fun saveUserExtended(name: String, surname: String, role: String) {
        prefs.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_SURNAME, surname)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getName(): String    = prefs.getString(KEY_NAME, "") ?: ""
    fun getSurname(): String = prefs.getString(KEY_SURNAME, "") ?: ""
    fun getRole(): String    = prefs.getString(KEY_ROLE, "USER") ?: "USER"
    fun isPremium(): Boolean = getRole() == "PREMIUM"

    // ── Language preference ────────────────────────────────────────────────────
    fun saveLanguage(lang: String) = prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    fun getLanguage(): String      = prefs.getString(KEY_LANGUAGE, "EN") ?: "EN"

    // ── Logout ─────────────────────────────────────────────────────────────────
    fun clear() = prefs.edit().clear().apply()
}