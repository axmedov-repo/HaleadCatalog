package com.halead.catalog.screens.main

import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.ui.events.MainUiEvent
import kotlinx.coroutines.flow.StateFlow

interface MainViewModel {
    val mainUiState: StateFlow<MainUiState>
    val isPerspectiveEnabled: StateFlow<Boolean>
    val loadingApplyMaterialState: StateFlow<Boolean>
    val currentCursorState: StateFlow<CursorData>
    val editorScreenState: StateFlow<EditorScreenState>
    fun onUiEvent(mainUiEvent: MainUiEvent)
}