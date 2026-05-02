package com.example.wisefox.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*
import com.example.wisefox.utils.SessionManager

@Composable
fun AIScreen(navController: NavController) {
    val isPremium = SessionManager.isPremium()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────
        Text(
            text = stringResource(R.string.ai_title),
            fontSize = 22.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WiseFoxOrangeDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isPremium) {
            PremiumAdviceList()
        } else {
            MemberPaywall()
        }
    }
}

// ── Premium ──────────────────────────────────────────────────────
@Composable
private fun PremiumAdviceList() {
    val label = stringResource(R.string.ai_advice_label)
    val advices = listOf(
        "$label 1" to stringResource(R.string.advice_save_10pct),
        "$label 2" to stringResource(R.string.advice_wait_24h),
        "$label 3" to stringResource(R.string.advice_track_small)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        advices.forEach { (title, body) ->
            AdviceCard(title = title, body = body)
        }
    }
}

// ── Member paywall ──────────────────────────────────────────────
@Composable
private fun MemberPaywall() {
    val label = stringResource(R.string.ai_advice_label)
    val previewAdvices = listOf(
        "$label 1" to stringResource(R.string.advice_save_10pct),
        "$label 2" to stringResource(R.string.advice_wait_24h),
        "$label 3" to stringResource(R.string.advice_track_small)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            previewAdvices.forEach { (title, body) ->
                AdviceCard(title = title, body = body)
            }
        }

        // Paywall card overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ai_paywall_message),
                    color = WiseFoxOrangeDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: navigate to upgrade */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WiseFoxOrangeDark)
                ) {
                    Text(
                        text = stringResource(R.string.ai_paywall_cta),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdviceCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WiseFoxSubCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WiseFoxOrangeDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 14.sp,
                color = TextPrimary
            )
        }
    }
}
