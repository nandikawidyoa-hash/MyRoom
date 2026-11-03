package com.ourspace.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ourspace.app.ui.theme.OurSpaceDesign
import com.ourspace.app.ui.theme.SoftRomanticTokens

/**
 * Reusable Soft Romantic Glass Card with subtle translucent gradient,
 * delicate border, and warm rose/plum shadow.
 */
@Composable
fun RomanticGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = OurSpaceDesign.dimens.cardCornerRadius,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = OurSpaceDesign.colors
    val shape = RoundedCornerShape(cornerRadius)

    val cardModifier = modifier
        .shadow(
            elevation = OurSpaceDesign.dimens.cardElevation,
            shape = shape,
            ambientColor = colors.shadowAmbient,
            spotColor = colors.shadowAmbient
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.glassSurface,
                    colors.cardSurface.copy(alpha = 0.92f)
                )
            )
        )
        .border(
            width = OurSpaceDesign.dimens.glassBorderWidth,
            color = colors.glassBorder,
            shape = shape
        )
        .let { base ->
            if (onClick != null) base.clickable(onClick = onClick) else base
        }
        .padding(OurSpaceDesign.dimens.spaceMD)

    Column(
        modifier = cardModifier,
        content = content
    )
}

/**
 * Primary Soft Romantic Pill Button with tactile scale animation and loading state.
 */
@Composable
fun RomanticPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(OurSpaceDesign.dimens.pillCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = OurSpaceDesign.colors.primaryAccent,
            contentColor = SoftRomanticTokens.Cream,
            disabledContainerColor = OurSpaceDesign.colors.primaryAccent.copy(alpha = 0.45f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = SoftRomanticTokens.Cream,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Reusable Soft Romantic TextField with rounded corners and subtle error message support.
 */
@Composable
fun RomanticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = OurSpaceDesign.colors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = colors.textMuted) }
            } else null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            isError = errorMessage != null,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.cardSurface,
                unfocusedContainerColor = colors.glassSurface,
                focusedBorderColor = colors.primaryAccent,
                unfocusedBorderColor = colors.glassBorder,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedLabelColor = colors.primaryAccent,
                unfocusedLabelColor = colors.textMuted
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Couple Header Widget displaying both partners' initials/names with a gentle pulsing heart.
 */
@Composable
fun CoupleUniverseHeader(
    myNickname: String,
    partnerNickname: String,
    daysTogether: Long,
    modifier: Modifier = Modifier
) {
    val colors = OurSpaceDesign.colors
    val infiniteTransition = rememberInfiniteTransition(label = "heartPulse")
    val heartScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScaleAnim"
    )

    RomanticGlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Two Hearts. One Little Universe.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = myNickname,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Together with",
                        tint = colors.heartPulse,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(heartScale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = partnerNickname,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = colors.secondaryAccent.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, colors.glassBorder)
            ) {
                Text(
                    text = "$daysTogether Days",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

enum class SyncState {
    SYNCED, SYNCING, OFFLINE_PENDING
}

/**
 * Sync status pill indicator for real-time & offline state visibility.
 */
@Composable
fun SyncStatusIndicator(
    syncState: SyncState,
    pendingCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val colors = OurSpaceDesign.colors
    val (icon, label, tint) = when (syncState) {
        SyncState.SYNCED -> Triple(Icons.Default.CloudDone, "Synced", colors.primaryAccent)
        SyncState.SYNCING -> Triple(Icons.Default.Sync, "Syncing...", colors.textPrimary)
        SyncState.OFFLINE_PENDING -> Triple(
            Icons.Default.CloudOff,
            if (pendingCount > 0) "Offline ($pendingCount pending)" else "Offline mode",
            colors.textMuted
        )
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.glassSurface,
        border = BorderStroke(1.dp, colors.glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary
            )
        }
    }
}

/**
 * Custom Canvas Minimalist Romantic Orbit Illustration for empty states & welcome headers.
 */
@Composable
fun LittleUniverseIllustration(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    val colors = OurSpaceDesign.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            // Soft outer glow rings
            drawCircle(
                color = colors.tertiaryAccent.copy(alpha = 0.45f),
                radius = size.minDimension * 0.46f,
                center = center
            )
            drawCircle(
                color = colors.secondaryAccent.copy(alpha = 0.55f),
                radius = size.minDimension * 0.34f,
                center = center
            )
            // Orbit ring
            drawCircle(
                color = colors.primaryAccent.copy(alpha = 0.6f),
                radius = size.minDimension * 0.38f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            // Two orbiting stars (Two Hearts in One Little Universe)
            drawCircle(
                color = colors.primaryAccent,
                radius = 6.dp.toPx(),
                center = Offset(center.x - size.minDimension * 0.27f, center.y - size.minDimension * 0.26f)
            )
            drawCircle(
                color = colors.textPrimary,
                radius = 5.dp.toPx(),
                center = Offset(center.x + size.minDimension * 0.27f, center.y + size.minDimension * 0.26f)
            )
            // Center heart path
            val heartSize = size.minDimension * 0.24f
            val path = Path().apply {
                moveTo(center.x, center.y + heartSize * 0.45f)
                cubicTo(
                    center.x - heartSize, center.y - heartSize * 0.1f,
                    center.x - heartSize * 0.55f, center.y - heartSize * 0.85f,
                    center.x, center.y - heartSize * 0.35f
                )
                cubicTo(
                    center.x + heartSize * 0.55f, center.y - heartSize * 0.85f,
                    center.x + heartSize, center.y - heartSize * 0.1f,
                    center.x, center.y + heartSize * 0.45f
                )
                close()
            }
            drawPath(path = path, color = colors.primaryAccent)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}
