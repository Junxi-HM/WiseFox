package com.example.wisefox.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.LoginUiState
import com.example.wisefox.viewmodels.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    // ── Google Sign-In launcher ───────────────────────────────────────────────
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // !! 将下面的字符串替换为你在 Google Cloud Console 的 Web Client ID !!
            .requestIdToken("427423059339-cmjgaq9je98kp8aggm4oeonstakaobu0.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.googleLogin(idToken)
            } else {
                // token 为空时静默失败，也可以给用户提示
            }
        } catch (e: ApiException) {
            // 用户取消或错误，可以选择忽略或展示错误
        }
    }

    // ── 导航副作用 ────────────────────────────────────────────────────────────
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) onLoginSuccess()
    }

    // ── 验证码弹窗 ────────────────────────────────────────────────────────────
    val needVerifyState = uiState as? LoginUiState.NeedVerifyCode
    if (needVerifyState != null) {
        val verifyError = remember { mutableStateOf<String?>(null) }
        // 监听 ApiError 以便把错误显示在弹窗内
        LaunchedEffect(uiState) {
            if (uiState is LoginUiState.ApiError) {
                verifyError.value = (uiState as LoginUiState.ApiError).message
            }
        }
        VerifyCodeDialog(
            email         = needVerifyState.email,
            isLoading     = false,
            errorMessage  = verifyError.value,
            onConfirm     = { code -> viewModel.verifyCode(needVerifyState.email, code) },
            onDismiss     = { viewModel.resetState() }
        )
    }

    // ── 全屏背景 ──────────────────────────────────────────────────────────────
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

            // ── 登录卡片 ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = WiseFoxOrangeDark.copy(alpha = 0.35f),
                        spotColor    = WiseFoxOrangeDark.copy(alpha = 0.35f)
                    ),
                shape  = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 30.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text(
                        text = stringResource(R.string.login_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.login_subtitle),
                        fontSize = 17.sp,
                        color = TextWhite.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Email ──────────────────────────────────────────────────
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.hint_email), fontSize = 20.sp) },
                        leadingIcon = { Icon(Icons.Filled.Email, null, tint = TextWhite) },
                        singleLine = true,
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
                                Text(
                                    (uiState as LoginUiState.EmailError).message,
                                    fontSize = 17.sp,
                                    color = Color(0xFFFC7D7D)
                                )
                        },
                        colors = wiseFoxTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Password ───────────────────────────────────────────────
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.hint_password), fontSize = 20.sp) },
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
                            viewModel.login()
                        }),
                        isError = uiState is LoginUiState.PasswordError,
                        supportingText = {
                            if (uiState is LoginUiState.PasswordError)
                                Text(
                                    (uiState as LoginUiState.PasswordError).message,
                                    fontSize = 17.sp,
                                    color = Color(0xFFFFE4C0)
                                )
                        },
                        colors = wiseFoxTextFieldColors()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 28.dp)
                    ) {
                        // ── Google 登录按钮 ────────────────────────────────────
                        Button(
                            onClick = {
                                // 每次点击前强制退出，避免缓存的账号干扰
                                googleSignInClient.signOut().addOnCompleteListener {
                                    googleLauncher.launch(googleSignInClient.signInIntent)
                                }
                            },
                            enabled = uiState !is LoginUiState.Loading,
                            modifier = Modifier
                                .wrapContentWidth()
                                .height(35.dp),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor   = Color(0xFF1f1f1f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, Color(0xFF747775))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google Logo",
                                    modifier = Modifier.size(23.dp)
                                )
                                Text(
                                    text = stringResource(R.string.btn_login_google),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        // 忘记密码
                        Text(
                            text = stringResource(R.string.forgot_password),
                            fontSize = 17.sp,
                            color = TextWhite.copy(alpha = 0.9f),
                            modifier = Modifier.clickable { /* TODO */ }
                        )
                    }

                    // ── 登录按钮 ───────────────────────────────────────────────
                    Button(
                        onClick = { viewModel.login() },
                        enabled = uiState !is LoginUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape  = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WiseFoxOrangeDark,
                            contentColor   = TextWhite
                        )
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_login),
                                fontSize = 25.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.btn_register),
                        fontSize = 17.sp,
                        color = TextWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRegister() }
                    )

                    // ── 错误 / 提示信息 ────────────────────────────────────────
                    when (uiState) {
                        is LoginUiState.ApiError -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (uiState as LoginUiState.ApiError).message,
                                color = Color(0xFFFFE4C0),
                                fontSize = 17.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        is LoginUiState.SuggestGoogle -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape  = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.18f)
                                )
                            ) {
                                Text(
                                    text = "No account found. Please sign in with Google to register.",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }

            // ── Fox mascot ────────────────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.ic_fox_mascot),
                contentDescription = "WiseFox mascot",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 25.dp, y = (-30).dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun wiseFoxTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedLabelColor = TextWhite,
    unfocusedLabelColor = TextWhite.copy(alpha = 0.75f),
    focusedBorderColor = TextWhite,
    unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
    cursorColor = TextWhite,
    focusedContainerColor = Color.White.copy(alpha = 0.15f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
    errorBorderColor = Color(0xFFFFD0A0),
    errorLabelColor = Color(0xFFFFD0A0),
    errorCursorColor = Color(0xFFFFD0A0),
)