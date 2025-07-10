package com.halead.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    color: Color = Color.LightGray,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier =
            modifier
                .background(
                    color.copy(alpha = 0.5f),
                    shape = shape
                )
                .shimmerEffect()
    )
}