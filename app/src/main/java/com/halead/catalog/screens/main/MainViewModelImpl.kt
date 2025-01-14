package com.halead.catalog.screens.main

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halead.catalog.data.RecentAction
import com.halead.catalog.data.RecentActions
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.data.states.toTrackedState
import com.halead.catalog.repository.main.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val mainRepository: MainRepository,
    private val recentActions: RecentActions
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())
    private var isStateReacting = false

    init {
        Log.d("RecentActionsLog", "init")
        viewModelScope.launch {
            getMaterials()
            observeIsMaterialApplied()
        }

        viewModelScope.launch {
            saveCurrentState()
        }
    }

    private suspend fun observeIsMaterialApplied() {
        Log.d("RecentActionsLog", "observeMaterialDependentChanges")
        mainUiState
            .map { state ->
                listOf(
                    state.imageBmp, state.selectedMaterial, state.polygonPoints, state.overlays
                )
            }
            .distinctUntilChanged()
            .collect { _ ->
                mainUiState.update {
                    it.copy(isMaterialApplied = isMaterialApplied())
                }
            }
    }

    private suspend fun isMaterialApplied(): Boolean {
        val overlayPoints = mainUiState.value.overlays.flatMap { it.regionPoints }
        return mainUiState.value.polygonPoints.all { it in overlayPoints }
    }

    private suspend fun saveCurrentState() {
        Log.d("RecentActionsLog", "Sent to save")
        mainUiState
            .map { it.toTrackedState() }
            .distinctUntilChanged()
            .collect {
                if (!isStateReacting && it.imageBmp != null) {
                    recentActions.act(
                        RecentAction.UiState(mainUiState.value)
                    )
                }
                updateUndoRedo()
            }
    }

    private fun updateUndoRedo() {
        val newCanUndo = recentActions.canUndo()
        val newCanRedo = recentActions.canRedo()

        if (mainUiState.value.canUndo != newCanUndo || mainUiState.value.canRedo != newCanRedo) {
            mainUiState.update {
                it.copy(canUndo = newCanUndo, canRedo = newCanRedo)
            }
        }
    }

    private fun getMaterials() {
        mainUiState.update {
            it.copy(materials = mainRepository.getMaterials())
        }
    }

    override fun selectMaterial(material: Int) {
        mainUiState.update {
            it.copy(selectedMaterial = material)
        }
    }

    override fun selectImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            isStateReacting = false
            mainUiState.update {
                it.copy(
                    imageBmp = bitmap?.asImageBitmap(),
                    overlays = emptyList(),
                    polygonPoints = emptyList()
                )
            }
        }
    }

    override fun selectFunction(function: FunctionData) {
        viewModelScope.launch {
            when (function.type) {
                FunctionsEnum.UNDO -> {
                    reAct(recentActions.undo(), true)
                }

                FunctionsEnum.REDO -> {
                    reAct(recentActions.redo(), false)
                }

                FunctionsEnum.CLEAR_LAYERS -> {
                    isStateReacting = false
                    mainUiState.update {
                        it.copy(
                            overlays = emptyList(),
                            polygonPoints = emptyList()
                        )
                    }
                }

                else -> {}
            }
        }
    }

    override fun selectCursor(cursorData: CursorData) {
        mainUiState.update { it.copy(currentCursor = cursorData) }
    }

    override fun addOverlay(overlayMaterial: OverlayMaterialModel) {
        viewModelScope.launch {
            isStateReacting = false
            mainUiState.update {
                it.copy(
                    overlays = it.overlays.plus(overlayMaterial)
                )
            }
        }
    }

    override fun addPolygonPoint(offset: Offset) {
        viewModelScope.launch {
            isStateReacting = false
            mainUiState.update {
                it.copy(
                    polygonPoints = it.polygonPoints.plus(offset)
                )
            }
        }
    }

    override fun updatePolygonPoint(index: Int, offset: Offset) {
        viewModelScope.launch {
            if (index in mainUiState.value.polygonPoints.indices) {
                isStateReacting = true
                mainUiState.update {
                    it.copy(
                        overlays = emptyList(),
                        polygonPoints = it.polygonPoints.toMutableList().apply {
                            this[index] = offset
                        }
                    )
                }
            }
        }
    }

    override fun memorizeUpdatedPolygonPoints() {
        viewModelScope.launch {
            isStateReacting = false
            mainUiState.update { it.copy() }
        }
    }

    override fun clearPolygonPoints() {
        viewModelScope.launch {
            isStateReacting = false
            mainUiState.update {
                it.copy(polygonPoints = emptyList())
            }
        }
    }

    private fun reAct(recentAction: RecentAction?, undo: Boolean) {
        isStateReacting = true
        Log.d("RecentActionsLog", "reAct recentAction=${recentAction}")
        when (recentAction) {
            is RecentAction.UiState -> {
                Log.d("RecentActionsLog", "reAct condition=${(mainUiState.value != recentAction.mainUiState)}")
                mainUiState.value = recentAction.mainUiState
            }

            null -> {
                if (undo) {
                    mainUiState.update {
                        it.copy(
                            imageBmp = null,
                            overlays = emptyList(),
                            polygonPoints = emptyList()
                        )
                    }
                }
            }
        }
    }

    override fun bringHistoryWork(workModel: WorkModel) {
//        saveCurrentWork()
        mainUiState.update {
            it.copy(
                imageBmp = workModel.baseImage.asImageBitmap(),
                overlays = workModel.overlays
            )
        }
    }
}
