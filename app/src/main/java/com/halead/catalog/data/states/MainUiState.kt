package com.halead.catalog.data.states

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.halead.catalog.data.models.OverlayMaterialModel

@Immutable
data class MainUiState(
    val imageBmp: ImageBitmap? = null,
    val materials: List<Int> = emptyList(),
    val selectedMaterial: Int? = null,
    val overlays: List<OverlayMaterialModel> = emptyList(),
    val polygonPoints: List<Offset> = emptyList(),
    val currentOverlay: OverlayMaterialModel? = null,
    val isMaterialApplied: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val trigger: Boolean = true,
) {
    val editorHasImage: Boolean = imageBmp != null
}

data class TrackedUiState(
    val imageBmp: ImageBitmap?,
    val overlays: List<OverlayMaterialModel>,
    val polygonPoints: List<Offset>,
    val trigger: Boolean,
)

fun MainUiState.toTrackedState() = TrackedUiState(
    imageBmp = this.imageBmp,
    overlays = this.overlays,
    polygonPoints = this.polygonPoints,
    trigger = this.trigger,
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