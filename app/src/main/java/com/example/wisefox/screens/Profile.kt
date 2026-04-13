package com.example.wisefox.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*

private val PremiumBadgeBg  = Color(0xFFFFA040)
private val SectionCardBg   = Color(0xFFFFF3CC)   // slightly warmer than WiseFoxSubCardBg
private val RowItemBg       = Color(0xFFFFFAEE)

@Composable
fun ProfileScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Page title ───────────────────────────────────────
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

        // ── Welcome + User card ──────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome).uppercase() + " User!",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = WiseFoxOrangeDark,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(WiseFoxOrangePale)
                        .border(2.dp, WiseFoxOrange, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wisefox_icon),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "USER",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = WiseFoxOrangeDark
                    )
                )
                Text(
                    text = "user.name@gmail.com",
                    fontSize = 13.sp,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Premium badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(PremiumBadgeBg)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PREMIUM MEMBER",
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Account Preferences section ──────────────────────
        Text(
            text = stringResource(R.string.account_preferences_capital),
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SectionCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Language row ────────────────────────────
                LanguagePreferenceRow()

                // ── Notifications row ───────────────────────
                NotificationsRow()

                // ── Shared Ledgers row ──────────────────────
                PreferenceArrowRow(
                    iconRes = R.drawable.ic_shared,
                    label = stringResource(R.string.shared_ledgers_capital),
                    onClick = { /* TODO: navigate */ }
                )

                // ── Security row ────────────────────────────
                PreferenceArrowRow(
                    iconRes = R.drawable.ic_security,
                    label = stringResource(R.string.security_capital),
                    onClick = { /* TODO: navigate */ }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LanguagePreferenceRow() {
    val languages = listOf("EN", "ES", "CN")
    var selected by remember { mutableStateOf("EN") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Icon + label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_language), // language icon fallback
                contentDescription = null,
                tint = WiseFoxOrangeDark,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.language_capital),
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
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
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isSelected) WiseFoxOrange else Color.Transparent)
                        .clickable { selected = lang }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = lang,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else WiseFoxOrangeDark
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsRow() {
    var enabled by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = null,
                tint = WiseFoxOrangeDark,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.notification_capital),
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = { enabled = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = WiseFoxOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun PreferenceArrowRow(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RowItemBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = WiseFoxOrangeDark,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
        }

        // Arrow chevron
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(WiseFoxOrangePale)
        ) {
            Text(
                text = ">",
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = WiseFoxOrangeDark
                )
            )
        }
    }
}