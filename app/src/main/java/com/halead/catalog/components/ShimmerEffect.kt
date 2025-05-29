package com.halead.catalog.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.shimmerEffect(
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000,
    shimmerColors: List<Color> = defaultShimmerColors()
): Modifier {
    val infiniteTransition = rememberInfiniteTransition()
    val translateAnimation = infiniteTransition.animateFloat(
        initialValue = -widthOfShadowBrush.toFloat(),
        targetValue = durationMillis.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ShimmerAnimation"
    )

    return drawBehind {
        drawRect(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(x = translateAnimation.value, y = 0f),
                end = Offset(x = translateAnimation.value + widthOfShadowBrush, y = angleOfAxisY),
            )
        )
    }
}

@Composable
private fun defaultShimmerColors(): List<Color> = listOf(
    Color.White.copy(alpha = 0.3f),
    Color.White.copy(alpha = 0.5f),
    Color.White.copy(alpha = 1.0f),
    Color.White.copy(alpha = 0.5f),
    Color.White.copy(alpha = 0.3f),
)
