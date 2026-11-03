package com.ourspace.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue

@Composable
fun OurSpaceTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val targetSemantic = when (themeMode) {
        AppThemeMode.LIGHT -> LightRomanticColors
        AppThemeMode.DARK -> DarkRomanticColors
        AppThemeMode.PINK -> PinkRomanticColors
        AppThemeMode.LAVENDER -> LavenderRomanticColors
        AppThemeMode.CREAM -> CreamRomanticColors
    }

    val animSpec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 350)
    val bgPrimary by animateColorAsState(targetSemantic.backgroundPrimary, animSpec, label = "bgPrimary")
    val bgSecondary by animateColorAsState(targetSemantic.backgroundSecondary, animSpec, label = "bgSecondary")
    val cardSurface by animateColorAsState(targetSemantic.cardSurface, animSpec, label = "cardSurface")
    val glassSurface by animateColorAsState(targetSemantic.glassSurface, animSpec, label = "glassSurface")
    val glassBorder by animateColorAsState(targetSemantic.glassBorder, animSpec, label = "glassBorder")
    val primaryAccent by animateColorAsState(targetSemantic.primaryAccent, animSpec, label = "primaryAccent")
    val secondaryAccent by animateColorAsState(targetSemantic.secondaryAccent, animSpec, label = "secondaryAccent")
    val tertiaryAccent by animateColorAsState(targetSemantic.tertiaryAccent, animSpec, label = "tertiaryAccent")
    val textPrimary by animateColorAsState(targetSemantic.textPrimary, animSpec, label = "textPrimary")
    val textMuted by animateColorAsState(targetSemantic.textMuted, animSpec, label = "textMuted")

    val animatedSemantic = targetSemantic.copy(
        backgroundPrimary = bgPrimary,
        backgroundSecondary = bgSecondary,
        cardSurface = cardSurface,
        glassSurface = glassSurface,
        glassBorder = glassBorder,
        primaryAccent = primaryAccent,
        secondaryAccent = secondaryAccent,
        tertiaryAccent = tertiaryAccent,
        textPrimary = textPrimary,
        textMuted = textMuted
    )

    val materialColorScheme: ColorScheme = if (targetSemantic.isDark) {
        darkColorScheme(
            primary = animatedSemantic.primaryAccent,
            onPrimary = SoftRomanticTokens.DarkText,
            primaryContainer = animatedSemantic.tertiaryAccent,
            onPrimaryContainer = animatedSemantic.textPrimary,
            secondary = animatedSemantic.secondaryAccent,
            onSecondary = animatedSemantic.textPrimary,
            background = animatedSemantic.backgroundPrimary,
            onBackground = animatedSemantic.textPrimary,
            surface = animatedSemantic.cardSurface,
            onSurface = animatedSemantic.textPrimary,
            surfaceVariant = animatedSemantic.backgroundSecondary,
            onSurfaceVariant = animatedSemantic.textMuted,
            outline = animatedSemantic.glassBorder
        )
    } else {
        lightColorScheme(
            primary = animatedSemantic.primaryAccent,
            onPrimary = SoftRomanticTokens.Cream,
            primaryContainer = animatedSemantic.tertiaryAccent,
            onPrimaryContainer = animatedSemantic.textPrimary,
            secondary = animatedSemantic.secondaryAccent,
            onSecondary = animatedSemantic.textPrimary,
            background = animatedSemantic.backgroundPrimary,
            onBackground = animatedSemantic.textPrimary,
            surface = animatedSemantic.cardSurface,
            onSurface = animatedSemantic.textPrimary,
            surfaceVariant = animatedSemantic.backgroundSecondary,
            onSurfaceVariant = animatedSemantic.textMuted,
            outline = animatedSemantic.glassBorder
        )
    }

    CompositionLocalProvider(
        LocalOurSpaceColors provides animatedSemantic,
        LocalOurSpaceDimens provides OurSpaceDimens()
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = OurSpaceTypography,
            shapes = OurSpaceShapes,
            content = content
        )
    }
}

object OurSpaceDesign {
    val colors: OurSpaceSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOurSpaceColors.current

    val dimens: OurSpaceDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalOurSpaceDimens.current
}
