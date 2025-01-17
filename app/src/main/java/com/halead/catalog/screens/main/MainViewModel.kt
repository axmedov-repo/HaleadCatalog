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
    val loadingApplyMaterial: StateFlow<Boolean>
    fun selectMaterial(material: Int)
    fun selectImage(bitmap: Bitmap?)
    fun selectFunction(function: FunctionData)
    fun selectCursor(cursorData: CursorData)
    fun applyMaterial()
    fun allOverlaysDrawn()
    fun bringHistoryWork(workModel: WorkModel)
    fun addPolygonPoint(offset: Offset)
    fun updatePolygonPoint(index: Int, offset: Offset)
    fun updatePolygonPoints(newPolygonPoints : List<Offset>)
    fun memorizeUpdatedPolygonPoints()
    fun clearPolygonPoints()
}