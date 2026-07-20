package com.speehive.speehiveaihub.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a deep, physical 3D push-down micro-animation (0.92f compression + 5dp down shift).
 */
fun Modifier.deep3DPress(
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
): Modifier = this.composed {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "deepPressScale"
    )

    val translationY by animateDpAsState(
        targetValue = if (isPressed && enabled) 5.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "deepPressTranslation"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.translationY = translationY.toPx()
        }
        .then(
            if (onClick != null && enabled) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}

/**
 * Adds deep 3D card depth (12dp high-contrast slate shadow + bottom extrusion bevel + top light rim).
 */
fun Modifier.deep3DCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 12.dp,
    spotColor: Color = Color(0x380F172A),
    topHighlight: Color = Color.White
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = spotColor,
        spotColor = spotColor
    )
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                CardSurface,
                ElevatedSurface,
                Color(0xFFE2E8F0)
            )
        )
    )
    .border(
        border = BorderStroke(1.5.dp, topHighlight),
        shape = shape
    )

/**
 * Standard 3D card modifier fallback
 */
fun Modifier.threeDCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 10.dp,
    spotColor: Color = Color(0x300F172A),
    topHighlight: Color = Color.White
): Modifier = deep3DCard(shape, elevation, spotColor, topHighlight)

/**
 * Standard 3D press fallback
 */
fun Modifier.tactile3DPress(
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
): Modifier = deep3DPress(interactionSource, onClick, enabled)
