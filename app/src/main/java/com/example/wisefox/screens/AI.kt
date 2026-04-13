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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wisefox.ui.theme.*

private val IS_PREMIUM = false

private val genericAdvices = listOf(
    "ADVICE 1" to "Try to save at least 10% of your monthly income.",
    "ADVICE 2" to "Avoid impulse purchases by waiting 24 hours before buying.",
    "ADVICE 3" to "Track every expense, even the small ones — they add up fast."
)

@Composable
fun AIScreen(navController: NavController) {
    val isPremium = IS_PREMIUM   // reemplaza con tu fuente de verdad

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // ── Header ──────────────────────────────────────────
        Text(
            text = "Artificial Intelligence",
            fontSize = 22.sp,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WiseFoxOrangeDark
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isPremium) {
            // ── PREMIUM: lista de consejos reales ────────────
            PremiumAdviceList()
        } else {
            // ── MEMBER: consejos borrosos + paywall ──────────
            MemberPaywall()
        }
    }
}

// ── Premium ──────────────────────────────────────────────────────

@Composable
private fun PremiumAdviceList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Aquí pondrás los consejos reales generados por tu IA
        AdviceCard(title = "ADVICE 1", body = "Use less money")
        AdviceCard(title = "ADVICE 2", body = "Do not waste money")
    }
}

// ── Member paywall ────────────────────────────────────────────────

@Composable
private fun MemberPaywall() {
    Box(modifier = Modifier.fillMaxSize()) {

        // Consejos genéricos borrosos de fondo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(6.dp),           // efecto blur sobre los consejos
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            genericAdvices.forEach { (title, body) ->
                AdviceCard(title = title, body = body)
            }
        }

        // Overlay gris semi-transparente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xAABDBDBD))   // gris con ~67 % opacidad
        )

        // Mensaje + botón centrado
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "You are not able\nto use this feature",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 24.sp
            )

            Button(
                onClick = { /* navegar a pantalla de upgrade */ },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "Upgrade to Premium",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ── Shared composable ─────────────────────────────────────────────

@Composable
fun AdviceCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                color = Color.DarkGray
            )
        }
    }
}