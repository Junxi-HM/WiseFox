package com.example.wisefox.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.LoginViewModel
import com.example.wisefox.viewmodels.LoginUiState
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Google 新用户注册页面
 * 通过 googleToken（Step 2 返回）+ 用户填写的信息完成注册
 */
@Composable
fun GoogleRegisterScreen(
    googleToken: String,
    email: String,
    viewModel: LoginViewModel,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var name     by remember { mutableStateOf("") }
    var surname  by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = uiState is LoginUiState.Loading
    val errorMessage = (uiState as? LoginUiState.ApiError)?.message

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFEDD7B), Color(0xFFFFE288),
                        Color(0xFFFFE490), Color(0xFFFFE9A7), Color(0xFFFFEEB9)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // ── 注册卡片 ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = WiseFoxOrangeDark.copy(alpha = 0.35f),
                        spotColor   = WiseFoxOrangeDark.copy(alpha = 0.35f)
                    ),
                shape  = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 30.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // 标题
                    Text(
                        text = "Complete Registration",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = email,
                        fontSize = 14.sp,
                        color = TextWhite.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("First Name", fontSize = 18.sp) },
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = TextWhite) },
                        singleLine = true,
                        keyboardOptions  = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions  = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Surname
                    OutlinedTextField(
                        value = surname,
                        onValueChange = { surname = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Last Name", fontSize = 18.sp) },
                        leadingIcon = { Icon(Icons.Filled.Person, null, tint = TextWhite) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username", fontSize = 18.sp) },
                        leadingIcon = { Icon(Icons.Filled.AlternateEmail, null, tint = TextWhite) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password", fontSize = 18.sp) },
                        leadingIcon = { Icon(Icons.Filled.Lock, null, tint = TextWhite) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextWhite.copy(alpha = 0.7f)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (!isLoading) doRegister(viewModel, googleToken, username, name, surname, password)
                        }),
                        colors = wiseFoxTextFieldColors()
                    )

                    // 错误提示
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFFE4C0),
                            fontSize = 14.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 注册按钮
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            doRegister(viewModel, googleToken, username, name, surname, password)
                        },
                        enabled = !isLoading && name.isNotBlank() && surname.isNotBlank()
                                && username.isNotBlank() && password.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WiseFoxOrangeDark,
                            contentColor   = TextWhite,
                            disabledContainerColor = WiseFoxOrangeDark.copy(alpha = 0.45f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Account", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Fox mascot ────────────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.ic_fox_mascot),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 25.dp, y = (-30).dp)
            )
        }
    }
}

private fun doRegister(
    viewModel: LoginViewModel,
    googleToken: String,
    username: String,
    name: String,
    surname: String,
    password: String
) {
    viewModel.registerWithGoogle(
        googleToken = googleToken,
        username    = username,
        name        = name,
        surname     = surname,
        password    = password
    )
}