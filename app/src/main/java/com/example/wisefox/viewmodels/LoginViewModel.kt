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
    object Idle             : LoginUiState()
    object Loading          : LoginUiState()
    object Success          : LoginUiState()
    data class EmailError   (val message: String) : LoginUiState()
    data class PasswordError(val message: String) : LoginUiState()
    data class ApiError     (val message: String) : LoginUiState()
    /** Email not in DB → suggest Google register */
    object SuggestGoogle    : LoginUiState()
    /** Google new user → needs to enter verification code */
    data class NeedVerifyCode(val email: String) : LoginUiState()
    /** Code verified → needs to complete registration */
    data class NeedRegister(val googleToken: String, val email: String) : LoginUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var email    by mutableStateOf("")
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // ── Email + password login ────────────────────────────────────────────────
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

                // ✅ Save token
                response.token?.let { SessionManager.saveToken(it) }

                // ✅ Save core user info (ID is critical!)
                SessionManager.saveUser(
                    id       = response.id,
                    username = response.username,
                    email    = response.email
                )

                // ✅ Save extended info
                SessionManager.saveUserExtended(
                    name    = response.name,
                    surname = response.surname,
                    role    = response.role ?: "USER"
                )

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
                        android.util.Log.d("LOGIN_DEBUG", "Google response: $result")
                        // ✅ Existing Google user — save everything
                        result["token"]?.let { SessionManager.saveToken(it) }

                        val userId = result["userId"]?.toLongOrNull() ?: -1L
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

                // ✅ New Google user — save everything after registration
                result["token"]?.let { SessionManager.saveToken(it) }

                val userId = result["userId"]?.toLongOrNull() ?: -1L
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
    }
}