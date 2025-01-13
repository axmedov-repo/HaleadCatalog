package com.halead.catalog.screens.main

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
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
import com.halead.catalog.repository.main.MainRepository
import com.halead.catalog.repository.work.WorkRepository
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
    private val workRepository: WorkRepository,
    private val recentActions: RecentActions
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())

    init {
        getMaterials()
        observeMaterialDependentChanges()
    }

    private fun observeMaterialDependentChanges() {
        viewModelScope.launch {
            mainUiState
                .map { state -> listOf(state.imageBmp, state.selectedMaterial, state.polygonPoints) }
                .distinctUntilChanged()
                .collect { _ ->
                    mainUiState.update {
                        it.copy(isMaterialApplied = false)
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
//        saveCurrentWork()
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
//        saveCurrentWork()
        recentActions.clearAll()
        mainUiState.update {
            it.copy(
                imageBmp = bitmap?.asImageBitmap(),
                overlays = emptyList(),
                canUndo = recentActions.canUndo(),
                canRedo = recentActions.canRedo(),
                polygonPoints = emptyList()
            )
        }
    }

    override fun selectFunction(function: FunctionData) {
        when (function.type) {
            FunctionsEnum.UNDO -> {
                reAct(recentActions.undo(), true)
            }

            FunctionsEnum.REDO -> {
                reAct(recentActions.redo(), false)
            }

            FunctionsEnum.CLEAR_LAYERS -> {
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

    override fun selectCursor(cursorData: CursorData) {
        mainUiState.update { it.copy(currentCursor = cursorData) }
    }

    override fun addOverlay(overlayMaterial: OverlayMaterialModel) {
        viewModelScope.launch {
            recentActions.act(RecentAction.OverlayMaterial(overlayMaterial))
            mainUiState.update {
                it.copy(
                    overlays = it.overlays.plus(overlayMaterial),
                    canUndo = recentActions.canUndo(),
                    canRedo = recentActions.canRedo(),
                    isMaterialApplied = true
                )
            }
        }
    }

    override fun addPolygonPoint(offset: Offset) {
        viewModelScope.launch {
            mainUiState.update {
                it.copy(
                    polygonPoints = it.polygonPoints.plus(offset)
                )
            }
            recentActions.act(RecentAction.PolygonPoints(mainUiState.value.polygonPoints))
        }
    }

    override fun updatePolygonPoint(index: Int, offset: Offset) {
        viewModelScope.launch {
            if (index in mainUiState.value.polygonPoints.indices) {
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
            recentActions.act(RecentAction.PolygonPoints(mainUiState.value.polygonPoints))
        }
    }

    override fun clearPolygonPoints() {
        viewModelScope.launch {
            mainUiState.update {
                it.copy(polygonPoints = emptyList())
            }
        }
    }

    private fun reAct(recentAction: RecentAction?, undo: Boolean) {
        when (recentAction) {
            is RecentAction.OverlayMaterial -> {
                if (undo) {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.minus(recentAction.overlayMaterial),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                } else {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.plus(recentAction.overlayMaterial),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                }
            }

            is RecentAction.OverlayMaterialsList -> {
                if (undo) {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.minus(recentAction.overlayMaterials.toSet()),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                } else {
                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.plus(recentAction.overlayMaterials.toSet()),
                            canUndo = recentActions.canUndo(),
                            canRedo = recentActions.canRedo()
                        )
                    }
                }
            }

            is RecentAction.PolygonPoints -> {
                mainUiState.update {
                    it.copy(
                        polygonPoints = recentAction.polygonPoints,
                        canUndo = recentActions.canUndo(),
                        canRedo = recentActions.canRedo()
                    )
                }
            }

            null -> {}
        }
    }

    private fun saveCurrentWork() {
        viewModelScope.launch {
            mainUiState.value.imageBmp?.let {
                var resultBitmap: Bitmap = it.asAndroidBitmap()
//                for (overlay in mainUiState.value.overlays) {
//                    resultBitmap = getClippedMaterial(
//                        materialBitmap = overlay.materialBitmap,
//                        regionPoints = overlay.regionPoints
//                    )
//                }

                workRepository.insert(
                    WorkModel(
                        baseImage = resultBitmap,
                        overlays = mainUiState.value.overlays
                    )
                )
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
