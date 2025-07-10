package com.halead.catalog.data.models

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class OverlayData(
    val id: Int = 0, // for work history with OverlayMaterialEntity
    val overlay: ImageBitmap,
    val material: OverlayMaterial?,
    val hasPerspective: Boolean,
    val polygonPoints: ImmutableList<Offset>,
    val polygonCenter: Offset,
    val holePoints: ImmutableList<List<Offset>> = persistentListOf(),
    val offset: Offset = Offset.Zero,
    val rotation: Float = 0f,
    val scale: Float = 1f
)

@Immutable
data class OverlayMaterial(
    val resId: Int,
    val bitmap: ImageBitmap
)