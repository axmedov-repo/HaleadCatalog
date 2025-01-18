package com.halead.catalog.screens.main

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.states.MainUiState
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val mainUiState: StateFlow<MainUiState>
    val loadingApplyMaterialState: StateFlow<Boolean>
    val currentCursorState: StateFlow<CursorData>
    fun selectMaterial(material: Int)
    fun selectImage(bitmap: Bitmap?)
    fun selectFunction(function: FunctionData)
    fun selectCursor(cursorData: CursorData)
    fun selectOverlay(overlay: OverlayMaterialModel)
    fun unselectCurrentOverlay()
    fun applyMaterial()
    fun allOverlaysDrawn()
    fun insertPolygonPoint(offset: Offset)
    fun updatePolygonPoint(index: Int, offset: Offset)
    fun extendPolygonPoints(offset: Offset)
    fun memorizeUpdatedPolygonPoints()
    fun updateCurrentOverlayPosition(overlayIndex :Int, dragAmount: Offset)
    fun clearPolygonPoints()
    fun bringHistoryWork(workModel: WorkModel)
}