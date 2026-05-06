package com.example.wisefox.viewmodels

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisefox.repository.AuthRepository
import com.example.wisefox.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────
sealed class LoginUiState {
    object Idle           : LoginUiState()
    object Loading        : LoginUiState()
    object Success        : LoginUiState()
    data class EmailError   (val message: String) : LoginUiState()
    data class PasswordError(val message: String) : LoginUiState()
    data class ApiError     (val message: String) : LoginUiState()
    /** When logging in by hand, the email address is not in DB → prompts you to register with Google */
    object SuggestGoogle  : LoginUiState()
    /** New Google users → Require verification code */
    data class NeedVerifyCode(val email: String) : LoginUiState()
    /** Verification code passed → Registration required */
    data class NeedRegister(val googleToken: String, val email: String) : LoginUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    // Form fields
    var email    by mutableStateOf("")
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // ── 手写登录 ──────────────────────────────────────────────────────────────
    fun login() {
        if (email.isBlank()) {
            _uiState.value = LoginUiState.EmailError("Please enter your email")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = LoginUiState.EmailError("Please enter a valid email")
            return
        }
        if (password.isBlank()) {
            _uiState.value = LoginUiState.PasswordError("Please enter your password")
            return
        }

        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val response = repo.login(email.trim(), password)
                response.token?.let { SessionManager.saveToken(it) }
                SessionManager.saveUser(response.id, response.username, response.email)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                if (e.message == "EMAIL_NOT_FOUND") {
                    _uiState.value = LoginUiState.SuggestGoogle
                } else {
                    _uiState.value = LoginUiState.ApiError(
                        e.message ?: "Login failed. Please try again."
                    )
                }
            }
        }
    }

    // ── Google Step 1 ─────────────────────────────────────────────────────────
    fun googleLogin(idToken: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val result = repo.googleLogin(idToken)
                when (result["status"]) {
                    "OK" -> {
                        result["token"]?.let { SessionManager.saveToken(it) }

                        val userId = result["userId"]?.toLongOrNull()
                            ?: result["token"]?.let { parseUserIdFromJwt(it) }
                            ?: -1L

                        SessionManager.saveUser(
                            id       = userId,
                            username = result["username"] ?: "",
                            email    = result["email"] ?: ""
                        )
                        SessionManager.saveUserExtended(
                            name    = result["name"] ?: "",
                            surname = result["surname"] ?: "",
                            role    = result["role"] ?: "USER"
                        )
                        _uiState.value = LoginUiState.Success
                    }
                    "VERIFY_REQUIRED" -> {
                        val email = result["email"] ?: ""
                        _uiState.value = LoginUiState.NeedVerifyCode(email)
                    }
                    else -> _uiState.value = LoginUiState.ApiError("Unexpected response from server")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.ApiError(
                    e.message ?: "Google login failed. Please try again."
                )
            }
        }
    }

    // ── Google Step 2 ─────────────────────────────────────────────────────────
    fun verifyCode(email: String, code: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val result = repo.verifyCode(email, code)
                val googleToken = result["googleToken"]
                    ?: throw Exception("Missing googleToken in response")
                _uiState.value = LoginUiState.NeedRegister(googleToken, email)
            } catch (e: Exception) {
                _uiState.value = LoginUiState.ApiError(
                    e.message ?: "Code verification failed."
                )
            }
        }
    }

    // ── Google Step 3 ─────────────────────────────────────────────────────────
    fun registerWithGoogle(
        googleToken: String,
        username: String,
        name: String,
        surname: String,
        password: String
    ) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                val result = repo.registerWithGoogle(googleToken, username, name, surname, password)

                result["token"]?.let { SessionManager.saveToken(it) }

                // Parse userId from token JWT payload as fallback
                val userId = result["userId"]?.toLongOrNull()
                    ?: result["token"]?.let { parseUserIdFromJwt(it) }
                    ?: -1L

                SessionManager.saveUser(
                    id       = userId,
                    username = result["username"] ?: username,
                    email    = result["email"] ?: ""
                )
                SessionManager.saveUserExtended(
                    name    = result["name"] ?: name,
                    surname = result["surname"] ?: surname,
                    role    = result["role"] ?: "USER"
                )

                _uiState.value = LoginUiState.Success

            } catch (e: Exception) {
                _uiState.value = LoginUiState.ApiError(
                    e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }
    fun resetState() {
        _uiState.value = LoginUiState.Idle
        email = ""
        password = ""
    }

    private fun parseUserIdFromJwt(token: String): Long? {
        return try {
            // JWT format: header.payload.signature, payload is Base64
            val payload = token.split(".")[1]
            // Complete Base64 padding
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = String(android.util.Base64.decode(padded, android.util.Base64.URL_SAFE))
            // The payload is JSON; retrieve the "sub" field.
            val json = org.json.JSONObject(decoded)
            json.getString("sub").toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }
}