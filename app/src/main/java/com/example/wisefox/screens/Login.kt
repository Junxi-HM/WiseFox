package com.example.wisefox.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.LoginViewModel
import com.example.wisefox.viewmodels.LoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    // Trigger navigation on success
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onLoginSuccess()
    }

    // ── Full-screen gradient background ──────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8E8),
                        Color(0xFFFFF0CC),
                        Color(0xFFFFE8B0)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Outer box to allow mascot to overlap card ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {

            // ── Login Card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp)             // leave room for mascot
                    .shadow(
                        elevation   = 20.dp,
                        shape       = RoundedCornerShape(32.dp),
                        ambientColor = WiseFoxOrangeDark.copy(alpha = 0.35f),
                        spotColor   = WiseFoxOrangeDark.copy(alpha = 0.35f)
                    ),
                shape  = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 36.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    // Title
                    Text(
                        text       = stringResource(R.string.login_title),
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text     = stringResource(R.string.login_subtitle),
                        fontSize = 14.sp,
                        color    = TextWhite.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Email field ───────────────────────────────────────────
                    OutlinedTextField(
                        value         = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text(stringResource(R.string.hint_email)) },
                        leadingIcon   = {
                            Icon(Icons.Filled.Email, contentDescription = null,
                                tint = TextWhite)
                        },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction    = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = uiState is LoginUiState.EmailError,
                        supportingText = {
                            if (uiState is LoginUiState.EmailError)
                                Text((uiState as LoginUiState.EmailError).message,
                                    color = Color(0xFFFFD0A0))
                        },
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Password field ────────────────────────────────────────
                    OutlinedTextField(
                        value         = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text(stringResource(R.string.hint_password)) },
                        leadingIcon   = {
                            Icon(Icons.Filled.Lock, contentDescription = null,
                                tint = TextWhite)
                        },
                        trailingIcon  = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextWhite.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        ),
                        isError = uiState is LoginUiState.PasswordError,
                        supportingText = {
                            if (uiState is LoginUiState.PasswordError)
                                Text((uiState as LoginUiState.PasswordError).message,
                                    color = Color(0xFFFFD0A0))
                        },
                        colors = wiseFoxTextFieldColors()
                    )

                    // ── Forgot password ───────────────────────────────────────
                    Text(
                        text     = stringResource(R.string.forgot_password),
                        fontSize = 13.sp,
                        color    = TextWhite.copy(alpha = 0.9f),
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp, bottom = 28.dp)
                            .clickable { /* TODO: forgot password */ }
                    )

                    // ── Login button ──────────────────────────────────────────
                    Button(
                        onClick   = { viewModel.login() },
                        enabled   = uiState !is LoginUiState.Loading,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape     = RoundedCornerShape(24.dp),
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = WiseFoxOrangeDark,
                            contentColor   = TextWhite
                        )
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                color     = TextWhite,
                                modifier  = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = stringResource(R.string.btn_login),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Register link ─────────────────────────────────────────
                    Text(
                        text      = stringResource(R.string.btn_register),
                        fontSize  = 13.sp,
                        color     = TextWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRegister() }
                    )

                    // ── API error snackbar-style ──────────────────────────────
                    if (uiState is LoginUiState.ApiError) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text     = (uiState as LoginUiState.ApiError).message,
                            color    = Color(0xFFFFD0A0),
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Fox mascot – overlaps top-left corner of card ─────────────────
            Image(
                painter            = painterResource(id = R.drawable.ic_fox_mascot),
                contentDescription = "WiseFox mascot",
                modifier           = Modifier
                    .size(76.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp)          // slightly inside left edge
            )
        }
    }
}

// ── Shared TextField color helper ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun wiseFoxTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor         = TextWhite,
    unfocusedTextColor       = TextWhite,
    focusedLabelColor        = TextWhite,
    unfocusedLabelColor      = TextWhite.copy(alpha = 0.75f),
    focusedBorderColor       = TextWhite,
    unfocusedBorderColor     = TextWhite.copy(alpha = 0.5f),
    cursorColor              = TextWhite,
    focusedContainerColor    = Color.White.copy(alpha = 0.15f),
    unfocusedContainerColor  = Color.White.copy(alpha = 0.10f),
    errorBorderColor         = Color(0xFFFFD0A0),
    errorLabelColor          = Color(0xFFFFD0A0),
    errorCursorColor         = Color(0xFFFFD0A0),
)