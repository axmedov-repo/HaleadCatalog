package com.halead.catalog.data.states

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.DefaultCursorData
import com.halead.catalog.data.models.OverlayMaterialModel

data class MainUiState(
    val imageBmp: ImageBitmap? = null,
    val materials: Map<String, Int> = emptyMap(),
    val selectedMaterial: Int? = null,
    val overlays: List<OverlayMaterialModel> = emptyList(),
    val polygonPoints: List<Offset> = emptyList(),
    val currentCursor: CursorData = DefaultCursorData,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isMaterialApplied: Boolean = false
)

data class TrackedUiState(
    val imageBmp: ImageBitmap?,
    val overlays: List<OverlayMaterialModel>,
    val polygonPoints: List<Offset>,
)

fun MainUiState.toTrackedState() = TrackedUiState(
    imageBmp = this.imageBmp,
    overlays = this.overlays,
    polygonPoints = this.polygonPoints
)

data class MaterialDependentState(
    val imageBmp: ImageBitmap?,
    val selectedMaterial: Int?,
    val polygonPoints: List<Offset>,
    val overlays: List<OverlayMaterialModel>
)


fun MainUiState.toMaterialDependentState() = MaterialDependentState(
    imageBmp = this.imageBmp,
    selectedMaterial = this.selectedMaterial,
    polygonPoints = this.polygonPoints,
    overlays = this.overlays
)