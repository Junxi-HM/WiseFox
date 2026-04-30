package com.example.wisefox.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.model.LedgerResponse
import com.example.wisefox.model.UserResponse
import com.example.wisefox.navigation.Screen
import com.example.wisefox.ui.theme.*
import com.example.wisefox.utils.LocaleHelper
import com.example.wisefox.utils.SessionManager
import com.example.wisefox.viewmodels.ProfileUiState
import com.example.wisefox.viewmodels.ProfileViewModel
import com.example.wisefox.viewmodels.ShareLedgerState

private val PremiumBadgeBg = Color(0xFFFFA040)
private val SectionCardBg  = Color(0xFFFFF3CC)
private val RowItemBg      = Color(0xFFFFFAEE)
private val DangerRed      = Color(0xFFE53935)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState         by viewModel.uiState.collectAsStateWithLifecycle()
    val shareState      by viewModel.shareLedgerState.collectAsStateWithLifecycle()
    val context         = LocalContext.current

    // Language state – read from SessionManager so it survives recompositions
    var selectedLanguage by remember { mutableStateOf(SessionManager.getLanguage()) }

    // Share ledger dialog state
    var showShareDialog  by remember { mutableStateOf(false) }
    var selectedLedger   by remember { mutableStateOf<LedgerResponse?>(null) }

    // ── Share dialog ────────────────────────────────────────────────────────────
    if (showShareDialog && selectedLedger != null) {
        val isPremium = (uiState as? ProfileUiState.Success)?.user?.role == "PREMIUM"
        ShareLedgerDialog(
            ledger      = selectedLedger!!,
            isPremium   = isPremium,
            shareState  = shareState,
            onSend      = { email ->
                viewModel.shareLedger(selectedLedger!!.id, email)
            },
            onDismiss   = {
                showShareDialog = false
                selectedLedger  = null
                viewModel.resetShareState()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Page title ───────────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.nav_profile).uppercase(),
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = WiseFoxOrangeDark,
                letterSpacing = 2.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WiseFoxOrangeDark)
                }
            }

            is ProfileUiState.Error -> {
                Text(
                    text  = state.message,
                    color = DangerRed,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.loadProfile() },
                    colors  = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark)
                ) { Text(stringResource(R.string.retry), color = Color.White) }
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    user             = state.user,
                    avatar           = state.avatar,
                    ledgers          = state.ledgers,
                    selectedLanguage = selectedLanguage,
                    navController    = navController,
                    onLanguageChange = { lang ->
                        selectedLanguage = lang
                        SessionManager.saveLanguage(lang)
                        LocaleHelper.setLocale(context, lang)
                    },
                    onShareLedger    = { ledger ->
                        selectedLedger  = ledger
                        showShareDialog = true
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile content (when data is loaded)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileContent(
    user: UserResponse,
    avatar: Bitmap?,
    ledgers: List<LedgerResponse>,
    selectedLanguage: String,
    navController: NavController,
    onLanguageChange: (String) -> Unit,
    onShareLedger: (LedgerResponse) -> Unit
) {
    val isPremium = user.role == "PREMIUM"

    // ── User card ─────────────────────────────────────────────────────────────
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "${stringResource(R.string.welcome).uppercase()} ${user.name.uppercase()}!",
                fontSize = 15.sp,
                style    = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = TextSecondary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Avatar
            Box(
                modifier        = Modifier.size(88.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (avatar != null) {
                    Image(
                        bitmap             = avatar.asImageBitmap(),
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(3.dp, WiseFoxOrangeDark, CircleShape)
                    )
                } else {
                    Box(
                        modifier        = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(WiseFoxOrangePale)
                            .border(3.dp, WiseFoxOrangeDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text     = user.name.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color    = WiseFoxOrangeDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text     = "${user.name} ${user.surname}",
                fontSize = 20.sp,
                style    = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color      = WiseFoxOrangeDark
                )
            )
            Text(
                text     = "@${user.username}",
                fontSize = 13.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text     = user.email,
                fontSize = 12.sp,
                color    = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Premium / User badge
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(PremiumBadgeBg)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text  = stringResource(R.string.premium_member),
                        fontSize = 11.sp,
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(WiseFoxOrangePale)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text  = stringResource(R.string.standard_member),
                        fontSize = 11.sp,
                        style    = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color      = WiseFoxOrangeDark,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Edit profile button
            Button(
                onClick = { navController.navigate(Screen.EditProfile.route) },
                shape   = RoundedCornerShape(14.dp),
                colors  = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
            ) {
                Icon(
                    painter             = painterResource(R.drawable.ic_edit),
                    contentDescription  = null,
                    tint                = Color.White,
                    modifier            = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text       = stringResource(R.string.edit_profile),
                    color      = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── Account Preferences ───────────────────────────────────────────────────
    Text(
        text     = stringResource(R.string.account_preferences_capital),
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        style    = MaterialTheme.typography.labelMedium.copy(
            fontWeight    = FontWeight.Bold,
            color         = TextSecondary,
            letterSpacing = 1.sp
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = SectionCardBg)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguagePreferenceRow(
                selected = selectedLanguage,
                onSelect = onLanguageChange
            )
            NotificationsRow()
            PreferenceArrowRow(
                iconRes = R.drawable.ic_shared,
                label   = stringResource(R.string.shared_ledgers_capital),
                onClick = { /* TODO: navigate to shared ledgers list */ }
            )
            PreferenceArrowRow(
                iconRes = R.drawable.ic_security,
                label   = stringResource(R.string.security_capital),
                onClick = { /* TODO: navigate */ }
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ── My Ledgers / Share section ────────────────────────────────────────────
    if (ledgers.isNotEmpty()) {
        Text(
            text     = stringResource(R.string.my_ledgers_capital),
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            style    = MaterialTheme.typography.labelMedium.copy(
                fontWeight    = FontWeight.Bold,
                color         = TextSecondary,
                letterSpacing = 1.sp
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            colors   = CardDefaults.cardColors(containerColor = SectionCardBg)
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ledgers.forEach { ledger ->
                    LedgerShareRow(ledger = ledger, onShare = { onShareLedger(ledger) })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ── Logout button ─────────────────────────────────────────────────────────
    OutlinedButton(
        onClick  = { /* TODO: logout */ },
        shape    = RoundedCornerShape(14.dp),
        border   = androidx.compose.foundation.BorderStroke(1.5.dp, DangerRed),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text       = stringResource(R.string.logout),
            color      = DangerRed,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// Ledger row with share button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LedgerShareRow(ledger: LedgerResponse, onShare: () -> Unit) {
    Row(
        modifier            = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = ledger.name,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = WiseFoxOrangeDark
            )
            Text(
                text     = ledger.currency,
                fontSize = 12.sp,
                color    = TextSecondary
            )
        }
        TextButton(onClick = onShare) {
            Icon(
                painter             = painterResource(R.drawable.ic_shared),
                contentDescription  = null,
                tint                = WiseFoxOrangeDark,
                modifier            = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text       = stringResource(R.string.share_capital),
                color      = WiseFoxOrangeDark,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Share Ledger Dialog
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ShareLedgerDialog(
    ledger: LedgerResponse,
    isPremium: Boolean,
    shareState: ShareLedgerState,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier        = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isPremium) WiseFoxOrangePale else Color(0xFFFFEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter             = painterResource(R.drawable.ic_shared),
                        contentDescription  = null,
                        tint                = if (isPremium) WiseFoxOrangeDark else DangerRed,
                        modifier            = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text       = stringResource(R.string.share_ledger_title),
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = WiseFoxOrangeDark,
                    textAlign  = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isPremium) {
                    // Not premium → show restriction message
                    Text(
                        text      = stringResource(R.string.share_premium_required),
                        fontSize  = 14.sp,
                        color     = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEEEE))
                            .padding(12.dp)
                    ) {
                        Text(
                            text      = stringResource(R.string.upgrade_to_premium_hint),
                            fontSize  = 13.sp,
                            color     = DangerRed,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick  = onDismiss,
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.close), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Premium → show share form
                    Text(
                        text      = stringResource(R.string.share_ledger_subtitle, ledger.name),
                        fontSize  = 13.sp,
                        color     = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = { Text(stringResource(R.string.recipient_email)) },
                        singleLine    = true,
                        shape         = RoundedCornerShape(14.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = WiseFoxOrangeDark,
                            focusedLabelColor    = WiseFoxOrangeDark,
                            cursorColor          = WiseFoxOrangeDark
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (shareState) {
                        is ShareLedgerState.Sending -> {
                            CircularProgressIndicator(
                                color    = WiseFoxOrangeDark,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        is ShareLedgerState.Sent -> {
                            Text(
                                text      = stringResource(R.string.share_sent_success),
                                color     = Color(0xFF4A9E6A),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick  = onDismiss,
                                shape    = RoundedCornerShape(14.dp),
                                colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.close), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        is ShareLedgerState.Error -> {
                            Text(
                                text      = shareState.message,
                                color     = DangerRed,
                                fontSize  = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier  = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick  = onDismiss,
                                    shape    = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text(stringResource(R.string.cancel)) }
                                Button(
                                    onClick  = { onSend(email.trim()) },
                                    shape    = RoundedCornerShape(14.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
                                    modifier = Modifier.weight(1f)
                                ) { Text(stringResource(R.string.retry), color = Color.White) }
                            }
                        }
                        else -> {
                            Row(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick  = onDismiss,
                                    shape    = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) { Text(stringResource(R.string.cancel)) }
                                Button(
                                    onClick  = { if (email.isNotBlank()) onSend(email.trim()) },
                                    shape    = RoundedCornerShape(14.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark),
                                    modifier = Modifier.weight(1f),
                                    enabled  = email.isNotBlank()
                                ) {
                                    Text(stringResource(R.string.send_capital), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Language row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LanguagePreferenceRow(selected: String, onSelect: (String) -> Unit) {
    val languages = listOf("EN", "ES", "CN")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter             = painterResource(id = R.drawable.ic_language),
                contentDescription  = null,
                tint                = WiseFoxOrangeDark,
                modifier            = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text  = stringResource(R.string.language_capital),
                fontSize = 14.sp,
                style    = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = WiseFoxOrangeDark
                )
            )
        }

        // Pill selector
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(WiseFoxOrangePale)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            languages.forEach { lang ->
                val isSelected = lang == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) WiseFoxOrangeDark else Color.Transparent)
                        .clickable { onSelect(lang) }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = lang,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (isSelected) Color.White else WiseFoxOrangeDark
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Notifications row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NotificationsRow() {
    var notificationsEnabled by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter             = painterResource(id = R.drawable.ic_notification),
                contentDescription  = null,
                tint                = WiseFoxOrangeDark,
                modifier            = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text  = stringResource(R.string.notification_capital),
                fontSize = 14.sp,
                style    = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = WiseFoxOrangeDark
                )
            )
        }
        Switch(
            checked         = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it },
            colors          = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = WiseFoxOrangeDark,
                uncheckedTrackColor = WiseFoxOrangePale
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generic arrow preference row
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PreferenceArrowRow(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment   = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter             = painterResource(id = iconRes),
                contentDescription  = null,
                tint                = WiseFoxOrangeDark,
                modifier            = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text  = label,
                fontSize = 14.sp,
                style    = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = WiseFoxOrangeDark
                )
            )
        }
        Icon(
            painter             = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription  = null,
            tint                = TextSecondary,
            modifier            = Modifier.size(20.dp)
        )
    }
}