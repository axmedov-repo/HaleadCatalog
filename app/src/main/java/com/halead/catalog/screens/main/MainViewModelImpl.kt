package com.halead.catalog.screens.main

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halead.catalog.data.RecentAction
import com.halead.catalog.data.RecentActions
import com.halead.catalog.data.Settings
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.DefaultCursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.data.models.WorkModel
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.data.states.toMaterialDependentState
import com.halead.catalog.data.states.toTrackedState
import com.halead.catalog.repository.main.MainRepository
import com.halead.catalog.utils.applyMaterialToPolygon
import com.halead.catalog.utils.applyMaterialToPolygonWithHoles
import com.halead.catalog.utils.applyMaterialToQuadrilateral
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.getTemporaryClippedOverlay
import com.halead.catalog.utils.isPointInPolygon
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.timberE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val mainRepository: MainRepository,
    private val recentActions: RecentActions,
    private val settings: Settings
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())
    override val loadingApplyMaterialState = MutableStateFlow(false)
    override val currentCursorState = MutableStateFlow(DefaultCursorData)

    private var isRecentActionSavingEnabled = true
    private var isUndoEnabled: Boolean = true
    private var isRedoEnabled: Boolean = true

    override val switchValue: StateFlow<Boolean> = settings.perspectiveSwitchValue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    override fun changeSwitchValue(value: Boolean) {
        viewModelScope.launch {
            settings.changePerspectiveSwitch(value)
        }
    }

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

    private fun isMaterialApplied(): Boolean {
        val overlayPoints = mainUiState.value.currentOverlay?.polygonPoints ?: return false
        @Suppress("ConvertArgumentToSet")
        return if (overlayPoints.isEmpty() || (mainUiState.value.polygonPoints.minus(overlayPoints).isNotEmpty())) {
            false
        } else {
            mainUiState.value.currentOverlay?.material == mainUiState.value.selectedMaterial
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
            currentCursorState.update { DefaultCursorData }
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

                FunctionsEnum.ADD_LAYER -> {
                    mainUiState.update {
                        it.copy(
                            polygonPoints = emptyList(),
                            currentOverlay = null
                        )
                    }
                }

                FunctionsEnum.CLEAR_LAYERS -> {
                    mainUiState.update {
                        it.copy(
                            overlays = if (it.currentOverlay == null && !isMaterialApplied()) {
                                it.overlays
                            } else if (it.overlays.size <= 1) {
                                emptyList()
                            } else {
                                it.overlays.minus(it.currentOverlay!!)
                            },
                            currentOverlay = null,
                            polygonPoints = emptyList()
                        )
                    }
                }

                FunctionsEnum.REMOVE_SELECTION -> {
                    mainUiState.value.polygonPoints.takeIf { it.isNotEmpty() }?.let { polygonPoints ->
                        var editingOverlay: OverlayMaterialModel? = null
                        var editingOverlayIndex: Int = -1
                        for (index in mainUiState.value.overlays.size - 1 downTo 0) {
                            val overlay = mainUiState.value.overlays[index]
                            if (isPointInPolygon(polygonPoints.first(), overlay.polygonPoints)) {
                                editingOverlay = overlay
                                editingOverlayIndex = index
                                break
                            }
                        }

                        editingOverlay?.let {
                            applyMaterialWithHole(editingOverlay, editingOverlayIndex, polygonPoints)
                        }
                    }
                }

                FunctionsEnum.MOVE_TO_BACK -> {
                    moveSelectedOverlayToBack()
                }

                FunctionsEnum.MOVE_TO_FRONT -> {
                    moveSelectedOverlayToFront()
                }

                else -> {}
            }
        }
    }

    private fun moveSelectedOverlayToBack() {
        viewModelScope.launch {
            mainUiState.value.overlays.takeIf { it.size >= 2 }?.let { overlays ->
                val selectedIdx = overlays.indexOf(mainUiState.value.currentOverlay)
                val swapIdx = selectedIdx - 1

                if (selectedIdx in 1 until overlays.size && swapIdx in overlays.indices) {
                    val selectedOverlay = overlays[selectedIdx]
                    val swapOverlay = overlays[swapIdx]
                    mainUiState.update {
                        it.copy(
                            overlays = overlays.toPersistentList()
                                .set(selectedIdx, swapOverlay)
                                .set(swapIdx, selectedOverlay)
                        )
                    }
                }
            }
        }
    }

    private fun moveSelectedOverlayToFront() {
        viewModelScope.launch {
            mainUiState.value.overlays.takeIf { it.size >= 2 }?.let { overlays ->
                val selectedIdx = overlays.indexOf(mainUiState.value.currentOverlay)
                val swapIdx = selectedIdx + 1

                if (selectedIdx in 0 until overlays.size - 1 && swapIdx in overlays.indices) {
                    val selectedOverlay = overlays[selectedIdx]
                    val swapOverlay = overlays[swapIdx]
                    mainUiState.update {
                        it.copy(
                            overlays = overlays.toPersistentList()
                                .set(selectedIdx, swapOverlay)
                                .set(swapIdx, selectedOverlay)
                        )
                    }
                }
            }
        }
    }

    override fun selectCursor(cursorData: CursorData) {
        currentCursorState.update { cursorData }
    }

    private fun needToKeepCurrentPolygons(): Boolean {
        val currentPolygon = mainUiState.value.polygonPoints
        if (currentPolygon.size < 3) return false
        val currentOverlay = mainUiState.value.currentOverlay ?: return true

        @Suppress("ConvertArgumentToSet")
        return currentOverlay.polygonPoints.minus(currentPolygon).isNotEmpty()
    }

    override fun selectOverlay(overlay: OverlayMaterialModel) {
        viewModelScope.launch {
            if (needToKeepCurrentPolygons()) {
                keepCurrentPolygonPointsWithoutMaterial()
            }
            timber("PIP_CHECK", "$overlay")
            mainUiState.update {
                it.copy(
                    polygonPoints = overlay.polygonPoints,
                    currentOverlay = overlay,
                    selectedMaterial = overlay.material
                )
            }
        }
    }

    override fun unselectCurrentOverlay() {
        viewModelScope.launch {
            if (needToKeepCurrentPolygons()) {
                keepCurrentPolygonPointsWithoutMaterial()
            }
            mainUiState.update {
                it.copy(
                    currentOverlay = null,
                    polygonPoints = emptyList(),
                    selectedMaterial = null
                )
            }
        }
    }

    private suspend fun keepCurrentPolygonPointsWithoutMaterial() = coroutineScope {
        val uiState = mainUiState.value

        val offsetOfOverlay = async(Dispatchers.Default) {
            findMinOffset(uiState.polygonPoints)
        }

        val appliedMaterialBitmap = async(Dispatchers.Default) {
            getTemporaryClippedOverlay(regionPoints = uiState.polygonPoints)
        }

        val newOverlay = OverlayMaterialModel(
            overlay = appliedMaterialBitmap.await(),
            polygonPoints = uiState.polygonPoints,
            material = -1,
            position = offsetOfOverlay.await()
        )

        mainUiState.update {
            it.copy(
                overlays = it.overlays.plus(newOverlay)
            )
        }
    }

    override fun applyMaterial() {
        viewModelScope.launch {
            val uiState = mainUiState.value

            if (!uiState.isMaterialApplied && uiState.polygonPoints.size >= 3) {
                loadingApplyMaterialState.value = true

                val selectedMaterialBmp = uiState.selectedMaterial?.let { material ->
                    mainRepository.getBitmap(material)
                        .filter { it.isSuccess } // Emit only successful results
                        .mapNotNull { it.getOrNull() } // Safely map non-null values
                        .firstOrNull()
                        ?: run {
                            timberE("No valid bitmap could be fetched.")
                            null
                        }
                }

                timber("Materials", "selectedMaterialBmp=$selectedMaterialBmp")
                if (selectedMaterialBmp != null) {
                    val offsetOfOverlay = async(Dispatchers.Default) {
                        findMinOffset(uiState.polygonPoints)
                    }

                    val appliedMaterialBitmap = async(Dispatchers.Default) {
                        if (switchValue.value) {
                            if (uiState.polygonPoints.size == 4) {
                                applyMaterialToQuadrilateral(
                                    polygonPoints = uiState.polygonPoints,
                                    materialBitmap = selectedMaterialBmp
                                )
                            } else {
                                applyMaterialToPolygon(
                                    polygonPoints = uiState.polygonPoints,
                                    materialBitmap = selectedMaterialBmp
                                )
                            }
                        } else {
                            getClippedMaterial(
                                outerPolygon = uiState.polygonPoints,
                                materialBitmap = selectedMaterialBmp
                            )
                        }
                    }

                    val newOverlay = OverlayMaterialModel(
                        overlay = appliedMaterialBitmap.await(),
                        polygonPoints = uiState.polygonPoints,
                        material = uiState.selectedMaterial,
                        position = offsetOfOverlay.await()
                    )

                    if (mainUiState.value.currentOverlay != null && mainUiState.value.currentOverlay!!.polygonPoints == newOverlay.polygonPoints) {
                        mainUiState.value.overlays.indexOf(mainUiState.value.currentOverlay).takeIf { it != -1 }?.let { index ->
                            mainUiState.update { state ->
                                state.copy(
                                    overlays = state.overlays.toPersistentList().set(index, newOverlay),
                                    currentOverlay = newOverlay
                                )
                            }
                        }
                    } else {
                        mainUiState.update {
                            it.copy(
                                overlays = it.overlays.plus(newOverlay),
                                currentOverlay = newOverlay
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyMaterialWithHole(editingOverlay: OverlayMaterialModel, index: Int, holePoints: List<Offset>) {
        viewModelScope.launch {
            if (holePoints.size >= 3) {
                loadingApplyMaterialState.value = true

                val selectedMaterialBmp = editingOverlay.material.let { material ->
                    mainRepository.getBitmap(material)
                        .filter { it.isSuccess } // Emit only successful results
                        .mapNotNull { it.getOrNull() } // Safely map non-null values
                        .firstOrNull()
                        ?: run {
                            timberE("No valid bitmap could be fetched.")
                            null
                        }
                }

                timber("Materials", "selectedMaterialBmp=$selectedMaterialBmp")

                if (selectedMaterialBmp != null) {
                    val appliedMaterialBitmap = async(Dispatchers.Default) {
                        if (switchValue.value) {
                            applyMaterialToPolygonWithHoles(
                                outerPolygon = editingOverlay.polygonPoints,
                                materialBitmap = selectedMaterialBmp,
                                holes = listOf(holePoints)
                            )
                        } else {
                            getClippedMaterial(
                                outerPolygon = editingOverlay.polygonPoints,
                                materialBitmap = selectedMaterialBmp,
                                holes = listOf(holePoints)
                            )
                        }
                    }

                    val newOverlay = OverlayMaterialModel(
                        overlay = appliedMaterialBitmap.await(),
                        polygonPoints = editingOverlay.polygonPoints,
                        holePoints = holePoints,
                        material = editingOverlay.material,
                        position = editingOverlay.position,
                    )

                    mainUiState.update { state ->
                        state.copy(
                            overlays = state.overlays.toPersistentList().set(index, newOverlay),
                            polygonPoints = emptyList()
                        )
                    }
                }
            }
        }
    }

    override fun allOverlaysDrawn() {
        viewModelScope.launch {
            timber("allOverlaysDrawn", "allOverlaysDrawn called")
            loadingApplyMaterialState.value = false
        }
    }

    override fun insertPolygonPoint(offset: Offset) {
        viewModelScope.launch {
            mainUiState.update { state ->
                state.copy(
                    overlays = state.currentOverlay?.let { state.overlays - it } ?: state.overlays,
                    currentOverlay = null,
                    polygonPoints = state.polygonPoints.plus(offset)
                )
            }
        }
    }

    override fun updatePolygonPoint(index: Int, offset: Offset) {
        if (index !in mainUiState.value.polygonPoints.indices) return

        viewModelScope.launch {
            isRecentActionSavingEnabled = false
            mainUiState.update { state ->
                state.copy(
                    overlays = state.currentOverlay?.let { state.overlays - it } ?: state.overlays,
                    currentOverlay = null,
                    polygonPoints = state.polygonPoints.toPersistentList().set(index, offset)
                )
            }
        }
    }

    override fun extendPolygonPoints(offset: Offset) {
        viewModelScope.launch {
            val points = mainUiState.value.polygonPoints

            if (points.size >= 3) {
                // Find the two closest points to the new offset
                val closestIndex = points.indices.minByOrNull { index ->
                    val nextIndex = (index + 1) % points.size
                    val segmentMidpoint = (points[index] + points[nextIndex]) / 2F
                    (segmentMidpoint - offset).getDistance()
                } ?: return@launch

                // Insert the new point between the two closest points
                val nextIndex = (closestIndex + 1) % points.size

                // Update the polygon points in the ViewModel
                mainUiState.update { state ->
                    state.copy(
                        overlays = state.currentOverlay?.let { state.overlays - it } ?: state.overlays,
                        currentOverlay = null,
                        polygonPoints = state.polygonPoints.toPersistentList().add(nextIndex, offset)
                    )
                }
            } else {
                // Add the point normally for the initial shape creation
                insertPolygonPoint(offset)
            }
        }
    }

    override fun memorizeUpdatedPolygonPoints() {
        viewModelScope.launch {
            isRecentActionSavingEnabled = true
            mainUiState.update { it.copy(trigger = !it.trigger) }
        }
    }

    override fun updateCurrentOverlayPosition(overlayIndex: Int, dragAmount: Offset) {
        if (overlayIndex !in mainUiState.value.overlays.indices) return

        viewModelScope.launch {
            isRecentActionSavingEnabled = false
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch
            val newPolygonPoints = async { currentOverlay.polygonPoints.map { it + dragAmount } }
            val newPosition = async { currentOverlay.position + dragAmount }
            val updatedOverlay = currentOverlay.copy(polygonPoints = newPolygonPoints.await(), position = newPosition.await())

            timber("PIP_CHECK", "updatedOverlay=$updatedOverlay")
            mainUiState.update { state ->
                state.copy(
                    overlays = state.overlays.toPersistentList().set(overlayIndex, updatedOverlay),
                    currentOverlay = updatedOverlay,
                    polygonPoints = newPolygonPoints.await()
                )
            }
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
                    loadingApplyMaterialState.value = true
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
