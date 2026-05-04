package com.example.wisefox.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*
import com.example.wisefox.viewmodels.ProfileViewModel
import com.example.wisefox.viewmodels.ProfileUiState
import com.example.wisefox.viewmodels.UpdateProfileState
import java.io.InputStream

private val SectionCardBg = Color(0xFFFFF3CC)
private val TextDark      = Color(0xFF3A2A00)
private val ErrorRed      = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by viewModel.updateProfileState.collectAsStateWithLifecycle()
    val context     = LocalContext.current

    val successState = uiState as? ProfileUiState.Success

    var name            by remember { mutableStateOf(successState?.user?.name ?: "") }
    var surname         by remember { mutableStateOf(successState?.user?.surname ?: "") }
    var username        by remember { mutableStateOf(successState?.user?.username ?: "") }
    var email           by remember { mutableStateOf(successState?.user?.email ?: "") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var avatarBitmap by remember { mutableStateOf<Bitmap?>(successState?.avatar) }
    var avatarBytes  by remember { mutableStateOf<ByteArray?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val stream: InputStream? = context.contentResolver.openInputStream(it)
            val bytes = stream?.readBytes()
            stream?.close()
            if (bytes != null) {
                avatarBytes  = bytes
                avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }

    // Navigate back on successful save
    LaunchedEffect(updateState) {
        if (updateState is UpdateProfileState.Success) {
            viewModel.resetUpdateState()
            navController.popBackStack()
        }
    }

    // Pre-fill fields once data loads
    LaunchedEffect(successState) {
        successState?.let {
            if (name.isBlank())       name     = it.user.name
            if (surname.isBlank())    surname  = it.user.surname
            if (username.isBlank())   username = it.user.username
            if (email.isBlank())      email    = it.user.email
            if (avatarBitmap == null) avatarBitmap = it.avatar
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Header with back arrow ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector        = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint               = WiseFoxOrangeDark
                )
            }
            Text(
                text       = stringResource(R.string.edit_profile),
                fontSize   = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = WiseFoxOrangeDark,
                modifier   = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Profile photo section ────────────────────────────────────────────
        SectionLabel(stringResource(R.string.profile_photo))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = SectionCardBg)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier        = Modifier
                        .size(110.dp)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap             = avatarBitmap!!.asImageBitmap(),
                            contentDescription = "Avatar",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, WiseFoxOrangeDark, CircleShape)
                        )
                    } else {
                        Box(
                            modifier        = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(WiseFoxOrangePale)
                                .border(3.dp, WiseFoxOrangeDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = name.firstOrNull()?.uppercase() ?: "?",
                                fontSize   = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = WiseFoxOrangeDark
                            )
                        }
                    }

                    // Camera badge
                    Box(
                        modifier         = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(WiseFoxOrangeDark)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter            = painterResource(R.drawable.ic_camera),
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text      = stringResource(R.string.tap_to_change_photo),
                    fontSize  = 12.sp,
                    color     = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Personal info section ────────────────────────────────────────────
        SectionLabel(stringResource(R.string.personal_info_capital))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = SectionCardBg)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WiseFoxTextField(
                    value          = name,
                    onValueChange  = { name = it },
                    label          = stringResource(R.string.hint_name),
                    leadingIconRes = R.drawable.ic_profile
                )
                WiseFoxTextField(
                    value          = surname,
                    onValueChange  = { surname = it },
                    label          = stringResource(R.string.hint_surname),
                    leadingIconRes = R.drawable.ic_profile
                )
                WiseFoxTextField(
                    value          = username,
                    onValueChange  = { username = it },
                    label          = stringResource(R.string.hint_username),
                    leadingIconRes = R.drawable.ic_profile
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Account info section ─────────────────────────────────────────────
        SectionLabel(stringResource(R.string.account_info_capital))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = SectionCardBg)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email field
                WiseFoxTextField(
                    value          = email,
                    onValueChange  = { email = it },
                    label          = stringResource(R.string.hint_email),
                    leadingIconRes = R.drawable.ic_email,
                    keyboardType   = KeyboardType.Email
                )

                // Password field with eye toggle
                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = { Text(stringResource(R.string.hint_new_password)) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(14.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Image(
                                painter            = painterResource(
                                    if (passwordVisible) R.drawable.ic_eye_off
                                    else R.drawable.ic_eye
                                ),
                                contentDescription = null,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    },
                    leadingIcon = {
                        Image(
                            painter            = painterResource(R.drawable.ic_eye_off),
                            contentDescription = null,
                            modifier           = Modifier.size(22.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = WiseFoxOrangeDark,
                        unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
                        focusedLabelColor    = WiseFoxOrangeDark,
                        unfocusedLabelColor  = WiseFoxOrangeDark.copy(alpha = 0.6f),
                        cursorColor          = WiseFoxOrangeDark,
                        focusedTextColor     = TextDark,
                        unfocusedTextColor   = TextDark
                    )
                )

                Text(
                    text     = stringResource(R.string.password_hint),
                    fontSize = 11.sp,
                    color    = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Error message ────────────────────────────────────────────────────
        if (updateState is UpdateProfileState.Error) {
            Text(
                text      = (updateState as UpdateProfileState.Error).message,
                color     = ErrorRed,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        // ── Save button ──────────────────────────────────────────────────────
        Button(
            onClick  = {
                // FIX #4: when password field is left blank, send null so the
                //          repository omits the multipart Part. Backend keeps the
                //          existing bcrypt hash → no more "Update Error 500".
                viewModel.updateProfile(
                    name     = name.trim(),
                    surname  = surname.trim(),
                    username = username.trim(),
                    email    = email.trim(),
                    password = password.takeIf { it.isNotBlank() },
                    pfpBytes = avatarBytes
                )
            },
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
            enabled  = updateState !is UpdateProfileState.Loading
        ) {
            if (updateState is UpdateProfileState.Loading) {
                CircularProgressIndicator(
                    color       = Color.White,
                    modifier    = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text       = stringResource(R.string.save_changes),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Cancel button ────────────────────────────────────────────────────
        OutlinedButton(
            onClick  = { navController.popBackStack() },
            shape    = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            border   = androidx.compose.foundation.BorderStroke(1.5.dp, WiseFoxOrangeDark)
        ) {
            Text(
                text       = stringResource(R.string.cancel),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = WiseFoxOrangeDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section label helper
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text     = text,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        style    = MaterialTheme.typography.labelMedium.copy(
            fontWeight    = FontWeight.Bold,
            color         = TextSecondary,
            letterSpacing = 1.sp
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable TextField — Image leading icon (no tint), dark text color
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WiseFoxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIconRes: Int,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        singleLine    = true,
        shape         = RoundedCornerShape(14.dp),
        modifier      = Modifier.fillMaxWidth(),
        leadingIcon   = {
            Image(
                painter            = painterResource(leadingIconRes),
                contentDescription = null,
                modifier           = Modifier.size(22.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = WiseFoxOrangeDark,
            unfocusedBorderColor = WiseFoxOrangeDark.copy(alpha = 0.4f),
            focusedLabelColor    = WiseFoxOrangeDark,
            unfocusedLabelColor  = WiseFoxOrangeDark.copy(alpha = 0.6f),
            cursorColor          = WiseFoxOrangeDark,
            focusedTextColor     = Color(0xFF3A2A00),
            unfocusedTextColor   = Color(0xFF3A2A00)
        )
    )
}
