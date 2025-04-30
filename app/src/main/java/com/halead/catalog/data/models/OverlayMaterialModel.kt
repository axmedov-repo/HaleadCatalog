package com.halead.catalog.data.models

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
data class OverlayMaterialModel(
    val id: Int = 0, // for work history with OverlayMaterialEntity
    val overlay: Bitmap,
    val material: Int,
    val hasPerspective: Boolean,
    val polygonPoints: List<Offset>,
    val polygonCenter: Offset,
    val holePoints: List<List<Offset>> = emptyList(),
    val offset: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val scale: Float = 1f
)
