package com.example.wisefox.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wisefox.R

val LunchBoxFont = FontFamily(
    Font(R.font.lunchbox, FontWeight.Normal)
)
val defaultTypography = Typography()
// Set of Material typography styles to start with
val Typography = Typography(
    // Display
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = LunchBoxFont),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = LunchBoxFont),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = LunchBoxFont),
    // Title
    titleLarge = TextStyle(
        fontFamily = LunchBoxFont,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = LunchBoxFont),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = LunchBoxFont),
    // Headline
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = LunchBoxFont),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = LunchBoxFont),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = LunchBoxFont),
    // Body
    bodyLarge = TextStyle(
        fontFamily = LunchBoxFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = LunchBoxFont),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = LunchBoxFont),
    // Label
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = LunchBoxFont),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = LunchBoxFont),
    labelSmall = TextStyle(
        fontFamily = LunchBoxFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)