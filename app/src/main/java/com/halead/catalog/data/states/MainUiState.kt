package com.halead.catalog.data.states

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import com.halead.catalog.data.models.OverlayData
import com.halead.catalog.data.models.OverlayMaterial
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MainUiState(
    val imageBmp: ImageBitmap? = null,
    val materials: ImmutableList<Int> = persistentListOf(),
    val selectedMaterial: OverlayMaterial? = null,
    val overlays: ImmutableList<OverlayData> = persistentListOf(),
    val polygonPoints: ImmutableList<Offset> = persistentListOf(),
    val currentOverlay: OverlayData? = null,
    val isMaterialApplied: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val trigger: Boolean = true,
) {
    val editorHasImage: Boolean = imageBmp != null
}

@Immutable
data class TrackedUiState(
    val imageBmp: ImageBitmap?,
    val overlays: ImmutableList<OverlayData>,
    val polygonPoints: ImmutableList<Offset>,
    val trigger: Boolean,
)

fun MainUiState.toTrackedState() = TrackedUiState(
    imageBmp = this.imageBmp,
    overlays = this.overlays,
    polygonPoints = this.polygonPoints,
    trigger = this.trigger,
)

@Immutable
data class MaterialDependentState(
    val imageBmp: ImageBitmap?,
    val selectedMaterial: OverlayMaterial?,
    val polygonPoints: ImmutableList<Offset>,
    val overlays: ImmutableList<OverlayData>
)

fun MainUiState.toMaterialDependentState() = MaterialDependentState(
    imageBmp = this.imageBmp,
    selectedMaterial = this.selectedMaterial,
    polygonPoints = this.polygonPoints,
    overlays = this.overlays
)