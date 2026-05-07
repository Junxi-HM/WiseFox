package com.example.wisefox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisefox.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG 1 — Enter email to receive the reset code
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Asks the user for their account email and triggers /api/auth/forgot-password.
 *
 * @param isLoading    true while the ViewModel is waiting for a response
 * @param errorMessage error message from the backend (null = no error)
 * @param onSend       callback with the email when the user taps "Send Code"
 * @param onDismiss    callback when the user taps "Cancel"
 */
@Composable
fun ForgotEmailDialog(
    isLoading:    Boolean,
    errorMessage: String?,
    onSend:       (email: String) -> Unit,
    onDismiss:    () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Title ─────────────────────────────────────────────────
                    Text(
                        text       = "Forgot Password?",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Subtitle ──────────────────────────────────────────────
                    Text(
                        text       = "Enter your account email and we'll send you a 6-digit reset code.",
                        fontSize   = 14.sp,
                        color      = TextWhite.copy(alpha = 0.80f),
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Email field ───────────────────────────────────────────
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Email", fontSize = 16.sp) },
                        leadingIcon   = { Icon(Icons.Filled.Email, null, tint = TextWhite) },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && !isLoading) onSend(email.trim())
                        }),
                        colors = wiseFoxTextFieldColors()
                    )

                    // ── Backend error ─────────────────────────────────────────
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = errorMessage,
                            color     = Color(0xFFFFE4C0),
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Send Code button ──────────────────────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            if (!isLoading) onSend(email.trim())
                        },
                        enabled  = email.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = Color.White,
                            contentColor           = WiseFoxOrangeDark,
                            disabledContainerColor = Color.White.copy(alpha = 0.4f),
                            disabledContentColor   = WiseFoxOrangeDark.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = WiseFoxOrangeDark,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Send Code",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Cancel button ─────────────────────────────────────────
                    TextButton(
                        onClick  = { if (!isLoading) onDismiss() },
                        enabled  = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "Cancel",
                            fontSize = 15.sp,
                            color    = TextWhite.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG 2 — Enter the 6-digit reset code
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Asks the user for the 6-digit code sent to their email and triggers /api/auth/verify-reset-code.
 *
 * @param email        email shown in the description (collected in dialog 1)
 * @param isLoading    true while the ViewModel is waiting for a response
 * @param errorMessage error message from the backend (null = no error)
 * @param onVerify     callback with the code when the user taps "Verify"
 * @param onDismiss    callback when the user taps "Cancel"
 */
@Composable
fun ResetCodeDialog(
    email:        String,
    isLoading:    Boolean,
    errorMessage: String?,
    onVerify:     (code: String) -> Unit,
    onDismiss:    () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Title ─────────────────────────────────────────────────
                    Text(
                        text       = "Enter Reset Code",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Subtitle ──────────────────────────────────────────────
                    Text(
                        text       = "We sent a 6-digit code to\n$email",
                        fontSize   = 14.sp,
                        color      = TextWhite.copy(alpha = 0.80f),
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Code input field ──────────────────────────────────────
                    OutlinedTextField(
                        value         = code,
                        onValueChange = {
                            // Only allow digits, max 6 characters
                            if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it
                        },
                        modifier        = Modifier.fillMaxWidth(),
                        label           = { Text("6-digit Code", fontSize = 16.sp) },
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors          = wiseFoxTextFieldColors(),
                        textStyle       = LocalTextStyle.current.copy(
                            textAlign     = TextAlign.Center,
                            fontSize      = 24.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 8.sp
                        )
                    )

                    // ── Backend error ─────────────────────────────────────────
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = errorMessage,
                            color     = Color(0xFFFFE4C0),
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Verify button ─────────────────────────────────────────
                    Button(
                        onClick  = { if (!isLoading) onVerify(code) },
                        enabled  = code.length == 6 && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = Color.White,
                            contentColor           = WiseFoxOrangeDark,
                            disabledContainerColor = Color.White.copy(alpha = 0.4f),
                            disabledContentColor   = WiseFoxOrangeDark.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = WiseFoxOrangeDark,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Verify",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Cancel button ─────────────────────────────────────────
                    TextButton(
                        onClick  = { if (!isLoading) onDismiss() },
                        enabled  = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "Cancel",
                            fontSize = 15.sp,
                            color    = TextWhite.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG 3 — Enter and confirm the new password
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Lets the user set and confirm a new password. Triggers /api/auth/reset-password.
 *
 * @param isLoading    true while the ViewModel is waiting for a response
 * @param errorMessage error message from the backend (null = no error)
 * @param onReset      callback with the new password when the user taps "Reset Password"
 * @param onDismiss    callback when the user taps "Cancel"
 */
@Composable
fun NewPasswordDialog(
    isLoading:    Boolean,
    errorMessage: String?,
    onReset:      (newPassword: String) -> Unit,
    onDismiss:    () -> Unit
) {
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Local validation flags
    val passwordTooShort  = password.isNotEmpty() && password.length < 6
    val passwordsMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val canSubmit         = password.length >= 6 && password == confirmPassword && !isLoading

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape  = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Title ─────────────────────────────────────────────────
                    Text(
                        text       = "New Password",
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // ── Subtitle ──────────────────────────────────────────────
                    Text(
                        text       = "Choose a strong password of at least 6 characters.",
                        fontSize   = 14.sp,
                        color      = TextWhite.copy(alpha = 0.80f),
                        textAlign  = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── New password field ────────────────────────────────────
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("New Password", fontSize = 16.sp) },
                        leadingIcon   = { Icon(Icons.Filled.Lock, null, tint = TextWhite) },
                        trailingIcon  = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector        = if (passwordVisible) Icons.Filled.Visibility
                                                        else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint               = TextWhite.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        singleLine     = true,
                        isError        = passwordTooShort,
                        supportingText = if (passwordTooShort) {
                            { Text("Minimum 6 characters", color = Color(0xFFFFE4C0), fontSize = 12.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Next
                        ),
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Confirm password field ────────────────────────────────
                    OutlinedTextField(
                        value         = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Confirm Password", fontSize = 16.sp) },
                        leadingIcon   = { Icon(Icons.Filled.Lock, null, tint = TextWhite) },
                        trailingIcon  = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector        = if (confirmVisible) Icons.Filled.Visibility
                                                        else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint               = TextWhite.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (confirmVisible) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        singleLine     = true,
                        isError        = passwordsMismatch,
                        supportingText = if (passwordsMismatch) {
                            { Text("Passwords don't match", color = Color(0xFFFFE4C0), fontSize = 12.sp) }
                        } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (canSubmit) onReset(password)
                        }),
                        colors = wiseFoxTextFieldColors()
                    )

                    // ── Backend error ─────────────────────────────────────────
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = errorMessage,
                            color     = Color(0xFFFFE4C0),
                            fontSize  = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Reset Password button ─────────────────────────────────
                    Button(
                        onClick  = {
                            focusManager.clearFocus()
                            if (canSubmit) onReset(password)
                        },
                        enabled  = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor         = Color.White,
                            contentColor           = WiseFoxOrangeDark,
                            disabledContainerColor = Color.White.copy(alpha = 0.4f),
                            disabledContentColor   = WiseFoxOrangeDark.copy(alpha = 0.4f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = WiseFoxOrangeDark,
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Reset Password",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Cancel button ─────────────────────────────────────────
                    TextButton(
                        onClick  = { if (!isLoading) onDismiss() },
                        enabled  = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text     = "Cancel",
                            fontSize = 15.sp,
                            color    = TextWhite.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}
