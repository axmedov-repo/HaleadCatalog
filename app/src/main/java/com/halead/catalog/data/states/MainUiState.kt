package com.halead.catalog.data.states

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.halead.catalog.R
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes

data class MainUiState(
    val imageBmp: ImageBitmap? = null,
    val materials: Map<String, Int> = emptyMap(),
    val selectedMaterial: Int? = null,
    val overlays: List<OverlayMaterialModel> = emptyList(),
    val polygonPoints : List<Offset> = emptyList(),
    val currentCursor: CursorData = CursorData(
        img = R.drawable.ic_draw,
        text = "Draw",
        type = CursorTypes.DRAW
    ),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isMaterialApplied : Boolean = false
)
