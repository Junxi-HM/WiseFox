package com.example.wisefox.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 用 SharedPreferences 持久化保存 JWT token 及用户基础信息。
 * 在 Application onCreate() 中调用 SessionManager.init(context) 完成初始化。
 */
object SessionManager {

    private const val PREF_NAME  = "wisefox_session"
    private const val KEY_TOKEN  = "jwt_token"
    private const val KEY_USER_ID    = "user_id"
    private const val KEY_USERNAME   = "username"
    private const val KEY_EMAIL      = "email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ── Token ─────────────────────────────────────────────────────────────────
    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?      = prefs.getString(KEY_TOKEN, null)
    fun isLoggedIn(): Boolean    = getToken() != null

    // ── User info ─────────────────────────────────────────────────────────────
    fun saveUser(id: Long, username: String, email: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getUserId(): Long    = prefs.getLong(KEY_USER_ID, -1L)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getEmail(): String    = prefs.getString(KEY_EMAIL, "") ?: ""

    // ── Logout ────────────────────────────────────────────────────────────────
    fun clear() = prefs.edit().clear().apply()
}