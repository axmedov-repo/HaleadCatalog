package com.halead.catalog.screens

import android.net.Uri
import com.halead.catalog.data.entity.OverlayMaterial
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.states.MainUiState
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val mainUiState: StateFlow<MainUiState>
    fun selectMaterial(material: Int)
    fun selectImage(uri: Uri?)
    fun selectFunction(function: FunctionData)
    fun selectCursor(cursorData: CursorData)
    fun addOverlay(overlayMaterial: OverlayMaterial)
}