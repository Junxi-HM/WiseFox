package com.example.wisefox.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import com.example.wisefox.repository.UserRepository
import com.example.wisefox.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── UI state sealed hierarchy ──────────────────────────────────────────────────
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: UserResponse,
        val avatar: Bitmap?,
        val ledgers: List<LedgerResponse>
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ShareLedgerState {
    object Idle : ShareLedgerState()
    object Sending : ShareLedgerState()
    object Sent : ShareLedgerState()
    data class Error(val message: String) : ShareLedgerState()
}

sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

class ProfileViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _shareLedgerState = MutableStateFlow<ShareLedgerState>(ShareLedgerState.Idle)
    val shareLedgerState: StateFlow<ShareLedgerState> = _shareLedgerState

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState

    // Current stored password (needed for update since backend requires it)
    private var currentPassword: String = ""

    init {
        loadProfile()
    }

    fun loadProfile() {
        val userId = SessionManager.getUserId()
        if (userId == -1L) {
            _uiState.value = ProfileUiState.Error("Not logged in")
            return
        }

        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                val user    = repo.getUser(userId)
                val ledgers = repo.getLedgers(userId)

                // Fetch avatar bitmap if available
                val avatar: Bitmap? = if (user.hasProfilePicture) {
                    try {
                        val body = repo.getProfilePicture(userId)
                        body?.bytes()?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    } catch (e: Exception) { null }
                } else null

                // Cache role & name in session for other screens
                SessionManager.saveUserExtended(
                    name     = user.name,
                    surname  = user.surname,
                    role     = user.role
                )

                _uiState.value = ProfileUiState.Success(user, avatar, ledgers)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // ── Share ledger by email ──────────────────────────────────────────────────
    fun shareLedger(ledgerId: Long, targetEmail: String) {
        val userId = SessionManager.getUserId()
        _shareLedgerState.value = ShareLedgerState.Sending
        viewModelScope.launch {
            try {
                repo.shareLedgerByEmail(userId, ledgerId, targetEmail)
                _shareLedgerState.value = ShareLedgerState.Sent
            } catch (e: Exception) {
                _shareLedgerState.value = ShareLedgerState.Error(e.message ?: "Share failed")
            }
        }
    }

    fun resetShareState() {
        _shareLedgerState.value = ShareLedgerState.Idle
    }

    // ── Update profile ─────────────────────────────────────────────────────────
    fun updateProfile(
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String,
        pfpBytes: ByteArray?
    ) {
        val userId = SessionManager.getUserId()
        _updateProfileState.value = UpdateProfileState.Loading
        viewModelScope.launch {
            try {
                val updated = repo.updateUser(
                    id       = userId,
                    name     = name,
                    surname  = surname,
                    username = username,
                    email    = email,
                    password = password,
                    pfpBytes = pfpBytes
                )
                // Update session
                SessionManager.saveUser(userId, updated.username, updated.email)
                SessionManager.saveUserExtended(updated.name, updated.surname, updated.role)
                _updateProfileState.value = UpdateProfileState.Success
                // Reload profile data
                loadProfile()
            } catch (e: Exception) {
                _updateProfileState.value = UpdateProfileState.Error(e.message ?: "Update failed")
            }
        }
    }

    fun resetUpdateState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }
}