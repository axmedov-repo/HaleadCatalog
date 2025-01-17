package com.halead.catalog.screens.main

import android.graphics.Bitmap
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
import com.halead.catalog.data.states.toMaterialDependentState
import com.halead.catalog.data.states.toTrackedState
import com.halead.catalog.repository.main.MainRepository
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.resizeBitmap
import com.halead.catalog.utils.timber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val mainRepository: MainRepository,
    private val recentActions: RecentActions
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())
    override val loadingApplyMaterial = MutableStateFlow(false)
    private var isRecentActionSavingEnabled = true
    private var isUndoEnabled: Boolean = true
    private var isRedoEnabled: Boolean = true

    init {
        timber("Materials", "init")
        viewModelScope.launch {
            getMaterials()
            observeIsMaterialApplied()
        }

        viewModelScope.launch {
            saveCurrentState()
        }
    }

    private suspend fun observeIsMaterialApplied() {
        mainUiState
            .map { it.toMaterialDependentState() }
            .distinctUntilChanged()
            .collect { _ ->
                mainUiState.update {
                    it.copy(isMaterialApplied = isMaterialApplied())
                }
            }
    }

    /*
    private fun isMaterialApplied(): Boolean {
        val overlayPoints = mainUiState.value.overlays.flatMap { it.regionPoints }
        return if (overlayPoints.isEmpty() || !mainUiState.value.polygonPoints.all { it in overlayPoints }) {
            false
        } else {
            mainUiState.value.overlays.last().material == mainUiState.value.selectedMaterial
        }
    }
    */

    private fun isMaterialApplied(): Boolean {
        val overlayPoints = mainUiState.value.overlays.flatMap { it.regionPoints }
        return if (overlayPoints.isEmpty() || (mainUiState.value.polygonPoints.minus(overlayPoints).isNotEmpty())) {
            false
        } else {
            mainUiState.value.overlays.last().material == mainUiState.value.selectedMaterial
        }
    }

    private suspend fun saveCurrentState() {
        mainUiState
            .filter { isRecentActionSavingEnabled }
            .map { it.toTrackedState() }
            .distinctUntilChanged()
            .collect {
                if (it.imageBmp != null) {
                    timber("RecentActionsLog", "State saved")
                    recentActions.act(RecentAction.UiState(mainUiState.value))
                }

                updateUndoRedo()
            }
    }

    private suspend fun updateUndoRedo() = coroutineScope {
        val newCanUndo = async { recentActions.canUndo() }
        val newCanRedo = async { recentActions.canRedo() }

        val canUndoValue = newCanUndo.await()
        val canRedoValue = newCanRedo.await()

        mainUiState.value.let { currentState ->
            if (currentState.canUndo != canUndoValue || currentState.canRedo != canRedoValue) {
                mainUiState.update {
                    it.copy(canUndo = canUndoValue, canRedo = canRedoValue)
                }
            }
        }
    }

    private fun getMaterials() {
        mainRepository.getMaterials().onEach { response ->
            response.onSuccess { result ->
                timber("Materials", "getMaterials()=$result")
                mainUiState.update {
                    it.copy(materials = result)
                }
            }
        }.launchIn(viewModelScope)
    }


    override fun selectMaterial(material: Int) {
        mainUiState.update {
            it.copy(selectedMaterial = material)
        }
    }

    override fun selectImage(bitmap: Bitmap?) {
        viewModelScope.launch {
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
                    if (isUndoEnabled) {
                        isUndoEnabled = false
                        launch {
                            delay(50)
                            isUndoEnabled = true
                        }
                        reAct(recentActions.undo(), true)
                    }
                }

                FunctionsEnum.REDO -> {
                    if (isRedoEnabled) {
                        isRedoEnabled = false
                        launch {
                            delay(50)
                            isRedoEnabled = true
                        }
                        reAct(recentActions.redo(), false)
                    }
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
    }

    override fun selectCursor(cursorData: CursorData) {
        mainUiState.update { it.copy(currentCursor = cursorData) }
    }

    override fun applyMaterial() {
        viewModelScope.launch {
            val uiState = mainUiState.value

            if (!uiState.isMaterialApplied && uiState.polygonPoints.isNotEmpty()) {
                loadingApplyMaterial.value = true

                val selectedMaterialBmp = uiState.selectedMaterial?.let { material ->
                    mainRepository.getBitmap(material)
                        .onEach { result ->
                            result.onFailure { error ->
                                timber("Error", "Failed to fetch bitmap: ${error.message}")
                            }
                        }
                        .mapNotNull { it.getOrNull() }
                        .firstOrNull()
                }

                timber("Materials", "selectedMaterialBmp=$selectedMaterialBmp")

                if (uiState.polygonPoints.isNotEmpty() && selectedMaterialBmp != null) {
                    val resizedBitmap = async(Dispatchers.Default) {
                        resizeBitmap(selectedMaterialBmp, 1024, 1024)
                    }

                    val offsetOfOverlay = async(Dispatchers.Default) {
                        findMinOffset(uiState.polygonPoints)
                    }

                    val appliedMaterialBitmap = async(Dispatchers.Default) {
                        getClippedMaterial(
                            materialBitmap = resizedBitmap.await(),
                            regionPoints = uiState.polygonPoints
                        )
                    }

                    mainUiState.update {
                        it.copy(
                            overlays = it.overlays.plus(
                                OverlayMaterialModel(
                                    overlay = appliedMaterialBitmap.await(),
                                    regionPoints = uiState.polygonPoints,
                                    material = uiState.selectedMaterial,
                                    position = offsetOfOverlay.await()
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    override fun allOverlaysDrawn() {
        viewModelScope.launch {
            loadingApplyMaterial.value = false
        }
    }

    override fun addPolygonPoint(offset: Offset) {
        viewModelScope.launch {
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
                isRecentActionSavingEnabled = false
                mainUiState.update {
                    it.copy(
                        overlays = emptyList(),
                        polygonPoints = it.polygonPoints.toPersistentList().set(index, offset)
                    )
                }
                isRecentActionSavingEnabled = true
            }
        }
    }

    override fun memorizeUpdatedPolygonPoints() {
        viewModelScope.launch {
            mainUiState.update { it.copy() }
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
        timber("RecentActionsLog", "reAct recentAction=${recentAction}")
        var newState = mainUiState.value
        when (recentAction) {
            is RecentAction.UiState -> {
                timber("RecentActionsLog", "reAct condition=${(mainUiState.value != recentAction.mainUiState)}")
                newState = recentAction.mainUiState
            }

            null -> {
                if (undo) {
                    newState = MainUiState(materials = mainUiState.value.materials)
                }
            }
        }

        if (newState != mainUiState.value) {
            mainUiState.update {
                if (newState.overlays.isNotEmpty() && newState.overlays != mainUiState.value.overlays) {
                    loadingApplyMaterial.value = true
                }
                newState
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
