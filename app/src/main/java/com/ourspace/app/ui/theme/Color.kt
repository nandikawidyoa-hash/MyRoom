package com.ourspace.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Core "Soft Romantic" Palette specified for OUR SPACE:
 * - Soft pink: #F6DCE8
 * - Lavender: #E8DFF5
 * - Cream: #FFF8ED
 * - Rose: #D99AB5
 * - Dark text: #493A46
 * - Muted text: #998A96
 */
object SoftRomanticTokens {
    val SoftPink = Color(0xFFF6DCE8)
    val Lavender = Color(0xFFE8DFF5)
    val Cream = Color(0xFFFFF8ED)
    val Rose = Color(0xFFD99AB5)
    val DarkText = Color(0xFF493A46)
    val MutedText = Color(0xFF998A96)

    // Supporting accent & status colors
    val DeepRose = Color(0xFFB87391)
    val SoftSage = Color(0xFFD8EAD8)
    val WarmAmber = Color(0xFFF7DFBA)
    valGentleCoral = Color(0xFFE58F8F)

    // Note custom colors (for Personal & Shared Notes)
    val NoteBlush = Color(0xFFFDF0F6)
    val NoteLilac = Color(0xFFF3EEFA)
    val NoteVanilla = Color(0xFFFFFBF2)
    val NoteMint = Color(0xFFEEF7F2)
    val NotePeach = Color(0xFFFFF1EC)
    val NoteSky = Color(0xFFEEF5FA)
}

enum class AppThemeMode(val displayName: String, val subtitle: String) {
    LIGHT("Soft Romantic (Light)", "Warm cream with soft pink & lavender accents"),
    DARK("Velvet Night (Dark)", "Deep plum night mode with warm rose highlights"),
    PINK("Blush Rose (Pink)", "Romantic rose & petal pink atmosphere"),
    LAVENDER("Dreamy Lilac (Lavender)", "Serene lavender mist with rose accents"),
    CREAM("Warm Ivory (Cream)", "Cozy parchment cream with subtle rose-gold")
}

@Immutable
data class OurSpaceSemanticColors(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val cardSurface: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val shadowAmbient: Color,
    val heartPulse: Color,
    val isDark: Boolean
)

val LightRomanticColors = OurSpaceSemanticColors(
    backgroundPrimary = SoftRomanticTokens.Cream,
    backgroundSecondary = SoftRomanticTokens.SoftPink.copy(alpha = 0.45f),
    cardSurface = Color(0xFFFFFFFF),
    glassSurface = Color(0xFFFFFFFF).copy(alpha = 0.78f),
    glassBorder = SoftRomanticTokens.SoftPink.copy(alpha = 0.85f),
    primaryAccent = SoftRomanticTokens.Rose,
    secondaryAccent = SoftRomanticTokens.Lavender,
    tertiaryAccent = SoftRomanticTokens.SoftPink,
    textPrimary = SoftRomanticTokens.DarkText,
    textMuted = SoftRomanticTokens.MutedText,
    shadowAmbient = SoftRomanticTokens.Rose.copy(alpha = 0.16f),
    heartPulse = Color(0xFFE07A9B),
    isDark = false
)

val DarkRomanticColors = OurSpaceSemanticColors(
    backgroundPrimary = Color(0xFF1B141A),
    backgroundSecondary = Color(0xFF241B22),
    cardSurface = Color(0xFF2B2129),
    glassSurface = Color(0xFF332730).copy(alpha = 0.78f),
    glassBorder = SoftRomanticTokens.Rose.copy(alpha = 0.28f),
    primaryAccent = SoftRomanticTokens.Rose,
    secondaryAccent = Color(0xFF534368),
    tertiaryAccent = Color(0xFF4A3240),
    textPrimary = Color(0xFFF6ECF2),
    textMuted = Color(0xFFB8A7B4),
    shadowAmbient = Color(0xFF000000).copy(alpha = 0.45f),
    heartPulse = SoftRomanticTokens.Rose,
    isDark = true
)

val PinkRomanticColors = OurSpaceSemanticColors(
    backgroundPrimary = Color(0xFFFDF2F7),
    backgroundSecondary = SoftRomanticTokens.SoftPink,
    cardSurface = Color(0xFFFFF9FC),
    glassSurface = SoftRomanticTokens.SoftPink.copy(alpha = 0.65f),
    glassBorder = SoftRomanticTokens.Rose.copy(alpha = 0.45f),
    primaryAccent = SoftRomanticTokens.Rose,
    secondaryAccent = SoftRomanticTokens.Lavender,
    tertiaryAccent = SoftRomanticTokens.Cream,
    textPrimary = SoftRomanticTokens.DarkText,
    textMuted = SoftRomanticTokens.MutedText,
    shadowAmbient = SoftRomanticTokens.Rose.copy(alpha = 0.22f),
    heartPulse = Color(0xFFD16B8F),
    isDark = false
)

val LavenderRomanticColors = OurSpaceSemanticColors(
    backgroundPrimary = Color(0xFFF7F4FC),
    backgroundSecondary = SoftRomanticTokens.Lavender,
    cardSurface = Color(0xFFFCFaff),
    glassSurface = SoftRomanticTokens.Lavender.copy(alpha = 0.65f),
    glassBorder = Color(0xFFD0C0EA),
    primaryAccent = Color(0xFFA589C9),
    secondaryAccent = SoftRomanticTokens.SoftPink,
    tertiaryAccent = SoftRomanticTokens.Rose,
    textPrimary = SoftRomanticTokens.DarkText,
    textMuted = SoftRomanticTokens.MutedText,
    shadowAmbient = Color(0xFFA589C9).copy(alpha = 0.20f),
    heartPulse = SoftRomanticTokens.Rose,
    isDark = false
)

val CreamRomanticColors = OurSpaceSemanticColors(
    backgroundPrimary = SoftRomanticTokens.Cream,
    backgroundSecondary = Color(0xFFF7EFE2),
    cardSurface = Color(0xFFFFFCF7),
    glassSurface = Color(0xFFFFFCF7).copy(alpha = 0.84f),
    glassBorder = Color(0xFFEAD9C9),
    primaryAccent = SoftRomanticTokens.Rose,
    secondaryAccent = SoftRomanticTokens.SoftPink,
    tertiaryAccent = SoftRomanticTokens.Lavender,
    textPrimary = SoftRomanticTokens.DarkText,
    textMuted = SoftRomanticTokens.MutedText,
    shadowAmbient = Color(0xFFC99E8A).copy(alpha = 0.18f),
    heartPulse = SoftRomanticTokens.Rose,
    isDark = false
)

val LocalOurSpaceColors = staticCompositionLocalOf { LightRomanticColors }
