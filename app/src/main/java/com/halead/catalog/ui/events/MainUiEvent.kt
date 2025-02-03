package com.halead.catalog.ui.events

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.models.WorkModel

sealed class MainUiEvent {
    data class ChangeSwitchValue(val value: Boolean) : MainUiEvent()
    data class AddMaterial(val bitmap: Bitmap?) : MainUiEvent()
    data class SelectMaterial(val material: Int) : MainUiEvent()
    data class SelectImage(val bitmap: Bitmap?) : MainUiEvent()
    data class SelectFunction(val function: FunctionData) : MainUiEvent()
    data class SelectCursor(val cursorData: CursorData) : MainUiEvent()
    data class SelectOverlay(val overlay: OverlayMaterialModel) : MainUiEvent()
    data object UnselectCurrentOverlay : MainUiEvent()
    data object ApplyMaterial : MainUiEvent()
    data object AllOverlaysDrawn : MainUiEvent()
    data class InsertPolygonPoint(val offset: Offset) : MainUiEvent()
    data class UpdatePolygonPoint(val index: Int, val offset: Offset) : MainUiEvent()
    data class ExtendPolygonPoints(val offset: Offset) : MainUiEvent()
    data object MemorizeUpdatedPolygonPoints : MainUiEvent()
    data class UpdateCurrentOverlayTransform(val zoomChange: Float, val offsetChange: Offset, val rotationChange: Float) : MainUiEvent()
    data class UpdateCurrentOverlayPosition(val overlayIndex: Int?, val dragAmount: Offset) : MainUiEvent()
    data object ClearPolygonPoints : MainUiEvent()
    data class BringHistoryWork(val workModel: WorkModel) : MainUiEvent()
}