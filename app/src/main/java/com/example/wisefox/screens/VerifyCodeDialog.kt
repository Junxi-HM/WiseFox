package com.example.wisefox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.wisefox.ui.theme.*

/**
 * 全屏遮罩弹窗：用户输入发送到邮箱的 6 位验证码
 * @param email       收件邮箱，显示在提示文字中
 * @param isLoading   ViewModel 处于 Loading 状态时禁用按钮并显示进度条
 * @param errorMessage 后端返回的错误信息，非空时显示
 * @param onConfirm   用户点击确认，回传 6 位 code
 * @param onDismiss   用户取消
 */
@Composable
fun VerifyCodeDialog(
    email: String,
    isLoading: Boolean,
    errorMessage: String?,
    onConfirm: (code: String) -> Unit,
    onDismiss: () -> Unit
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
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = WiseFoxLoginCardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = "Verify Your Email",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 说明
                    Text(
                        text = "We sent a 6-digit code to\n$email",
                        fontSize = 14.sp,
                        color = TextWhite.copy(alpha = 0.80f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 验证码输入框
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("6-digit Code", fontSize = 16.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = wiseFoxTextFieldColors(),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp
                        )
                    )

                    // 错误信息
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFFE4C0),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // 确认按钮
                    Button(
                        onClick = { onConfirm(code) },
                        enabled = code.length == 6 && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WiseFoxOrangeDark,
                            contentColor = TextWhite,
                            disabledContainerColor = WiseFoxOrangeDark.copy(alpha = 0.45f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Confirm", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 取消
                    TextButton(onClick = { if (!isLoading) onDismiss() }) {
                        Text(
                            text = "Cancel",
                            fontSize = 15.sp,
                            color = TextWhite.copy(alpha = 0.70f)
                        )
                    }
                }
            }
        }
    }
}