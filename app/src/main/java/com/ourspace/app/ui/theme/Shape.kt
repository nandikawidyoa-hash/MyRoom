package com.ourspace.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val OurSpaceShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Immutable
data class OurSpaceDimens(
    val spaceXXS: Dp = 4.dp,
    val spaceXS: Dp = 8.dp,
    val spaceSM: Dp = 12.dp,
    val spaceMD: Dp = 16.dp,
    val spaceLG: Dp = 24.dp,
    val spaceXL: Dp = 32.dp,
    val spaceXXL: Dp = 48.dp,
    val cardCornerRadius: Dp = 24.dp,
    val pillCornerRadius: Dp = 50.dp,
    val cardElevation: Dp = 6.dp,
    val glassBorderWidth: Dp = 1.dp
)

val LocalOurSpaceDimens = staticCompositionLocalOf { OurSpaceDimens() }
