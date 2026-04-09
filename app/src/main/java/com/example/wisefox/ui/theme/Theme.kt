package com.example.wisefox.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand Colors ──────────────────────────────────────────────────────────────
val WiseFoxOrangeDark = Color(0xFFE8732A)
val WiseFoxOrange = Color(0xFFF0924A)
val WiseFoxOrangeLight = Color(0xFFF5B07A)
val WiseFoxOrangePale = Color(0xFFFCDBB0)
val WiseFoxBgYellow = Color(0xFFFFF3D6)
val WiseFoxBgLight = Color(0xFFFFFBF5)
val WiseFoxLoginCardBg = Color(0xFFF5A86E)
val WiseFoxCardBg = Color(0xFFFDF3D6)
val WiseFoxSubCardBg = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF2D1A0A)
val TextSecondary = Color(0xFF7A5C40)
val TextHint = Color(0xFFB89070)
val TextWhite = Color(0xFFFFFFFF)
val Divider = Color(0xFFF0DCC8)

// ── Color Scheme ──────────────────────────────────────────────────────────────
private val WiseFoxColorScheme = lightColorScheme(
    primary = WiseFoxOrange,
    onPrimary = TextWhite,
    primaryContainer = WiseFoxOrangePale,
    secondary = WiseFoxOrangeLight,
    onSecondary = TextPrimary,
    background = WiseFoxBgYellow,
    surface = WiseFoxCardBg,
    onSurface = TextWhite,
    onBackground = TextPrimary,
)

// ── Shapes ────────────────────────────────────────────────────────────────────
val WiseFoxShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(32.dp),
)

// ── Theme entry point ─────────────────────────────────────────────────────────
@Composable
fun WiseFoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WiseFoxColorScheme,
        shapes = WiseFoxShapes,
        content = content,
        typography = Typography,
    )
}