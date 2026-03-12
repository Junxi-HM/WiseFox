package com.example.wisefox.viewmodels

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────
sealed class LoginUiState {
    object Idle          : LoginUiState()
    object Loading       : LoginUiState()
    object Success       : LoginUiState()
    data class EmailError   (val message: String) : LoginUiState()
    data class PasswordError(val message: String) : LoginUiState()
    data class ApiError     (val message: String) : LoginUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────
class LoginViewModel : ViewModel() {

    // Form fields as Compose state (observed directly in the composable)
    var email    by mutableStateOf("")
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login() {
        // ── Validation ────────────────────────────────────────────────────────
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

        // ── Network call ──────────────────────────────────────────────────────
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            try {
                // TODO: Replace with your Retrofit repository call, e.g.:
                // val response = authRepository.login(email.trim(), password)
                // SessionManager.saveToken(response.token)
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.ApiError(
                    e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}