package org.vita3k.emulator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.vita3k.emulator.R

val PlexSans = FontFamily(
    Font(R.font.plex_sans_regular, FontWeight.Normal),
    Font(R.font.plex_sans_medium, FontWeight.Medium),
    Font(R.font.plex_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plex_sans_bold, FontWeight.Bold)
)

val PlexMono = FontFamily(
    Font(R.font.plex_mono_regular, FontWeight.Normal),
    Font(R.font.plex_mono_medium, FontWeight.Medium)
)

private fun sans(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = PlexSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.em
)

val AppTypography = Typography(
    displayLarge = sans(48, 54, FontWeight.SemiBold, -0.02),
    displayMedium = sans(40, 46, FontWeight.SemiBold, -0.02),
    displaySmall = sans(34, 40, FontWeight.SemiBold, -0.015),
    headlineLarge = sans(30, 36, FontWeight.SemiBold, -0.015),
    headlineMedium = sans(26, 32, FontWeight.SemiBold, -0.01),
    headlineSmall = sans(22, 28, FontWeight.SemiBold, -0.005),
    titleLarge = sans(19, 25, FontWeight.SemiBold),
    titleMedium = sans(16, 22, FontWeight.Medium),
    titleSmall = sans(14, 20, FontWeight.SemiBold),
    bodyLarge = sans(15, 22, FontWeight.Normal),
    bodyMedium = sans(14, 20, FontWeight.Normal),
    bodySmall = sans(12, 17, FontWeight.Normal, 0.005),
    labelLarge = sans(14, 20, FontWeight.Medium),
    labelMedium = sans(12, 16, FontWeight.Medium, 0.01),
    labelSmall = sans(11, 14, FontWeight.Medium, 0.03)
)

/** Title IDs, versions, sizes and other machine-shaped values. */
val MonoSmall = TextStyle(
    fontFamily = PlexMono,
    fontWeight = FontWeight.Medium,
    fontSize = 11.5.sp,
    lineHeight = 15.sp
)

val MonoMedium = TextStyle(
    fontFamily = PlexMono,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp
)

/** Small uppercase labels over groups of content. */
val Eyebrow = TextStyle(
    fontFamily = PlexMono,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.08.em
)
