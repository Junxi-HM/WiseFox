package com.example.wisefox.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay
import com.example.wisefox.R
import com.example.wisefox.ui.theme.*

// ── Splash screen ─────────────────────────────────────────────────────────────
// Shown briefly on app launch before navigating to Login.
// Background matches the Login screen gradient (WiseFoxOrange family).
// Center: app logo + spinning arc + "WiseFox" brand name.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Auto-navigate after 2.5 seconds
    LaunchedEffect(Unit) {
        delay(2500L)
        onSplashFinished()
    }

    // ── Background gradient — same palette as Login ───────────────────────────
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF0924A), // WiseFoxOrange
            Color(0xFFE8732A), // WiseFoxOrangeDark
            Color(0xFFD45E1A)  // slightly deeper at bottom
        )
    )

    // ── Infinite spinner rotation ─────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash_spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 360f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(
        modifier           = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment   = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Logo ──────────────────────────────────────────────────────────
            // Wrap logo + spinner together so the arc surrounds the icon
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier.size(160.dp)
            ) {
                // Spinning arc
                SpinnerArc(
                    size     = 160.dp,
                    rotation = rotation
                )

                // App icon / logo centred inside the arc
                Image(
                    painter            = painterResource(id = R.drawable.ic_wisefox_icon),
                    contentDescription = "WiseFox logo",
                    modifier           = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Brand name ────────────────────────────────────────────────────
            Text(
                text       = "WiseFox",
                fontFamily = LunchBoxFont,
                fontWeight = FontWeight.Normal,
                fontSize   = 42.sp,
                color      = TextWhite,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Tagline (optional, soft) ───────────────────────────────────────
            Text(
                text       = "Smart money, clever moves",
                fontFamily = LunchBoxFont,
                fontWeight = FontWeight.Normal,
                fontSize   = 18.sp,
                color      = TextWhite.copy(alpha = 0.70f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ── Spinning arc composable ───────────────────────────────────────────────────
// Draws a partial circle arc that rotates continuously.
// Uses Canvas so it integrates cleanly with Compose.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpinnerArc(
    size       : Dp,
    rotation   : Float,
    strokeWidth: Dp = 4.dp,
    arcColor   : Color = TextWhite.copy(alpha = 0.90f)
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .rotate(rotation)
    ) {
        val strokePx = strokeWidth.toPx()
        val padding  = strokePx / 2f
        val arcSize  = Size(
            width  = this.size.width  - strokePx,
            height = this.size.height - strokePx
        )
        val topLeft  = Offset(padding, padding)

        // Main arc — 270° sweep, leaving a gap
        drawArc(
            color       = arcColor,
            startAngle  = 0f,
            sweepAngle  = 270f,
            useCenter   = false,
            topLeft     = topLeft,
            size        = arcSize,
            style       = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Short trailing arc (fade/ghost effect)
        drawArc(
            color       = arcColor.copy(alpha = 0.30f),
            startAngle  = 270f,
            sweepAngle  = 60f,
            useCenter   = false,
            topLeft     = topLeft,
            size        = arcSize,
            style       = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}
