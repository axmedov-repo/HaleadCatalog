package com.halead.catalog.data.states

import androidx.compose.ui.graphics.ImageBitmap
import com.halead.catalog.R
import com.halead.catalog.data.entity.OverlayMaterial
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes

data class MainUiState(
    val imageBmp: ImageBitmap? = null,
    val materials: Map<String, Int> = emptyMap(),
    val selectedMaterial: Int? = null,
    val overlays: List<OverlayMaterial> = emptyList(),
    val currentCursor: CursorData = CursorData(
        img = R.drawable.ic_draw,
        text = "Draw",
        type = CursorTypes.DRAW
    ),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
)
