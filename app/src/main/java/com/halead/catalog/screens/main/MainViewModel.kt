package com.halead.catalog.screens.main

import androidx.compose.ui.unit.IntSize
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.ui.events.MainUiEvent
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val mainUiState: StateFlow<MainUiState>
    val isPerspectiveEnabled: StateFlow<Boolean>
    val loadingApplyMaterialState: StateFlow<Boolean>
    val currentCursorState: StateFlow<CursorData>
    val editorSize: StateFlow<IntSize>
    val isEditorFullScreen: StateFlow<Boolean>
    fun onUiEvent(mainUiEvent: MainUiEvent)
}