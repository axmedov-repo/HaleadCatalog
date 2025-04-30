package com.halead.catalog.screens.main

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.utils.applyMaterialToPolygon
import com.halead.catalog.utils.applyMaterialToPolygonWithHoles
import com.halead.catalog.utils.applyMaterialToQuadrilateral
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.doPolygonsIntersect
import com.halead.catalog.utils.dpToPx
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.findPolygonCenter
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.getTemporaryClippedOverlay
import com.halead.catalog.utils.isQuadrilateral
import com.halead.catalog.utils.rotatePoints
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.timberE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

private val WIDTH_DIFF_OF_EDITOR_FULL_SCREEN: Dp = 88.dp
private val HEIGHT_DIFF_OF_EDITOR_FULL_SCREEN: Dp = 66.dp

@HiltViewModel
class MainViewModelImpl @Inject constructor(
    private val mainRepository: MainRepository,
    private val recentActions: RecentActions,
    private val settings: Settings,
    @ApplicationContext private val context: Context,
) : MainViewModel, ViewModel() {
    override val mainUiState = MutableStateFlow(MainUiState())
    override val loadingApplyMaterialState = MutableStateFlow(false)
    override val currentCursorState = MutableStateFlow(DefaultCursorData)
    override val isPerspectiveEnabled: StateFlow<Boolean> = settings.perspectiveSwitchValue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var isRecentActionSavingEnabled = true
    private var isUndoEnabled: Boolean = true
    private var isRedoEnabled: Boolean = true

    override val isEditorFullScreen = MutableStateFlow(false)
    private var fullScreenEditorSize: IntSize = IntSize.Zero
    private var normalEditorSize: IntSize = IntSize.Zero
    private var hasEditorSizeMeasured = false
    override val editorSize = MutableStateFlow(normalEditorSize)

    init {
        viewModelScope.launch {
            getMaterials()
            observeIsMaterialApplied()
        }

        viewModelScope.launch {
            saveCurrentState()
        }
    }

    override fun onUiEvent(mainUiEvent: MainUiEvent) {
        when (mainUiEvent) {
            is MainUiEvent.AddMaterial -> addMaterial(bitmap = mainUiEvent.bitmap)
            MainUiEvent.AllOverlaysDrawn -> allOverlaysDrawn()
            MainUiEvent.ApplyMaterial -> applyMaterial()
            is MainUiEvent.BringHistoryWork -> bringHistoryWork(workModel = mainUiEvent.workModel)
            is MainUiEvent.ChangePerspective -> changePerspective(value = mainUiEvent.value)
            MainUiEvent.ClearPolygonPoints -> clearPolygonPoints()
            is MainUiEvent.ExtendPolygonPoints -> extendPolygonPoints(offset = mainUiEvent.offset)
            is MainUiEvent.InsertPolygonPoint -> insertPolygonPoint(offset = mainUiEvent.offset)
            MainUiEvent.MemorizeUpdatedPolygonPoints -> memorizeUpdatedPolygonPoints()
            is MainUiEvent.SelectCursor -> selectCursor(cursorData = mainUiEvent.cursorData)
            is MainUiEvent.SelectFunction -> selectFunction(function = mainUiEvent.function)
            is MainUiEvent.SelectImage -> selectImage(bitmap = mainUiEvent.bitmap)
            is MainUiEvent.SelectMaterial -> selectMaterial(material = mainUiEvent.material)
            is MainUiEvent.SelectOverlay -> selectOverlay(overlay = mainUiEvent.overlay)
            MainUiEvent.UnselectCurrentOverlay -> unselectCurrentOverlay()
            is MainUiEvent.UpdateCurrentOverlayPosition -> updateCurrentOverlayPosition(
                overlayIndex = mainUiEvent.overlayIndex,
                dragAmount = mainUiEvent.dragAmount
            )

            is MainUiEvent.UpdateCurrentOverlayTransform -> updateCurrentOverlayTransform(
                zoomChange = mainUiEvent.zoomChange,
                offsetChange = mainUiEvent.offsetChange,
                rotationChange = mainUiEvent.rotationChange
            )

            is MainUiEvent.UpdatePolygonPoint -> updatePolygonPoint(
                index = mainUiEvent.index,
                offset = mainUiEvent.offset
            )

            is MainUiEvent.EditorSize -> {
                if (!hasEditorSizeMeasured) {
                    mainUiEvent.size.takeIf { it != normalEditorSize }?.let {
                        normalEditorSize = it
                        fullScreenEditorSize = IntSize(
                            width = it.width + dpToPx(context, WIDTH_DIFF_OF_EDITOR_FULL_SCREEN.value),
                            height = it.height + dpToPx(context, HEIGHT_DIFF_OF_EDITOR_FULL_SCREEN.value)
                        )
                        updateEditorSize()
                    }
                    hasEditorSizeMeasured = true
                }
            }

            MainUiEvent.ChangeEditorScreenSize -> {
                isEditorFullScreen.update { !it }
                updateEditorSize()
            }
        }
    }

    private fun updateEditorSize() {
        editorSize.update {
            if (isEditorFullScreen.value) fullScreenEditorSize else normalEditorSize
        }
    }

    private fun changePerspective(value: Boolean) {
        viewModelScope.launch {
            settings.changePerspective(value)
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
            mainUiState.value.currentOverlay?.material == mainUiState.value.selectedMaterial &&
                    mainUiState.value.currentOverlay?.hasPerspective == isPerspectiveEnabled.value
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

    private fun addMaterial(bitmap: Bitmap?) {
        viewModelScope.launch {
            mainRepository.addMaterial(bitmap)
        }
    }

    private fun selectMaterial(material: Int) {
        mainUiState.update {
            it.copy(selectedMaterial = material)
        }
    }

    private fun selectImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            mainUiState.update {
                it.copy(
                    imageBmp = bitmap?.asImageBitmap(),
                    overlays = emptyList(),
                    polygonPoints = emptyList(),
                    currentOverlay = null
                )
            }
            currentCursorState.update { DefaultCursorData }
        }
    }

    private fun selectFunction(function: FunctionData) {
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
                            overlays = if (it.currentOverlay == null && !it.isMaterialApplied) {
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
                    mainUiState.value.polygonPoints.takeIf { it.isNotEmpty() }
                        ?.let { polygonToRemove ->
                            val editingOverlays = HashMap<Int, OverlayMaterialModel>()

                            for (index in mainUiState.value.overlays.size - 1 downTo 0) {
                                val overlay = mainUiState.value.overlays[index]
                                if (doPolygonsIntersect(
                                        polygonToRemove,
                                        overlay.polygonPoints,
                                        overlay.holePoints
                                    )
                                ) {
                                    editingOverlays[index] = overlay
                                }
                            }

                            if (editingOverlays.isNotEmpty()) {
                                applyMaterialWithHole(editingOverlays, polygonToRemove)
                            }
                        }
                }

                FunctionsEnum.MOVE_TO_BACK -> {
                    moveSelectedOverlayToBack()
                }

                FunctionsEnum.MOVE_TO_FRONT -> {
                    moveSelectedOverlayToFront()
                }

                FunctionsEnum.ROTATE_LEFT -> {
                    rotateSelectedOverlay(-1f)
                }

                FunctionsEnum.ROTATE_RIGHT -> {
                    rotateSelectedOverlay(1f)
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

    private fun rotateSelectedOverlay(rotationAmount: Float) {
        viewModelScope.launch {
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch

            // First rotate the polygon points around their center
            val newPolygonPoints =
                async {
                    rotatePoints(
                        currentOverlay.polygonPoints,
                        rotationAmount,
                        currentOverlay.polygonCenter
                    )
                }

            // Keep the original offset
            val newOverlay = currentOverlay.copy(
                rotation = currentOverlay.rotation + rotationAmount,
                polygonPoints = newPolygonPoints.await()
                // Don't update the offset here
            )

            mainUiState.update {
                it.copy(
                    overlays = it.overlays.toPersistentList()
                        .set(it.overlays.indexOf(currentOverlay), newOverlay),
                    currentOverlay = newOverlay,
                    polygonPoints = newPolygonPoints.await()
                )
            }
        }
    }

    private fun selectCursor(cursorData: CursorData) {
        currentCursorState.update { cursorData }
    }

    private fun needToKeepCurrentPolygons(): Boolean {
        val currentPolygon = mainUiState.value.polygonPoints
        if (!currentPolygon.canMakeClosedShape()) return false
        val currentOverlay = mainUiState.value.currentOverlay ?: return true

        @Suppress("ConvertArgumentToSet")
        return currentOverlay.polygonPoints.minus(currentPolygon).isNotEmpty()
    }

    private fun selectOverlay(overlay: OverlayMaterialModel) {
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

    private fun unselectCurrentOverlay() {
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

        val polygonCenter = async { findPolygonCenter(uiState.polygonPoints) }

        val appliedMaterialBitmap = async(Dispatchers.Default) {
            getTemporaryClippedOverlay(regionPoints = uiState.polygonPoints)
        }

        val newOverlay = OverlayMaterialModel(
            overlay = appliedMaterialBitmap.await(),
            polygonPoints = uiState.polygonPoints,
            polygonCenter = polygonCenter.await(),
            material = -1,
            hasPerspective = isPerspectiveEnabled.value,
            offset = offsetOfOverlay.await()
        )

        mainUiState.update {
            it.copy(
                overlays = it.overlays.plus(newOverlay)
            )
        }
    }

    private fun applyMaterial() {
        viewModelScope.launch {
            val uiState = mainUiState.value

            if ((!uiState.isMaterialApplied || uiState.polygonPoints != uiState.currentOverlay?.polygonPoints) && uiState.polygonPoints.canMakeClosedShape()) {
                loadingApplyMaterialState.value = true
                val isEditingCurrentOverlay =
                    uiState.currentOverlay != null && (uiState.currentOverlay.polygonPoints.minus(
                        uiState.polygonPoints.toSet()
                    ).isEmpty())

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

                    val polygonCenter = async { findPolygonCenter(uiState.polygonPoints) }

                    val appliedMaterialBitmap = async(Dispatchers.Default) {
                        when {
                            isPerspectiveEnabled.value -> {
                                when {
                                    isEditingCurrentOverlay && uiState.currentOverlay!!.holePoints.isNotEmpty() ->
                                        applyMaterialToPolygonWithHoles(
                                            outerPolygon = uiState.polygonPoints,
                                            holes = uiState.currentOverlay.holePoints,
                                            materialBitmap = selectedMaterialBmp
                                        )

                                    uiState.polygonPoints.isQuadrilateral() ->
                                        applyMaterialToQuadrilateral(
                                            polygonPoints = uiState.polygonPoints,
                                            materialBitmap = selectedMaterialBmp
                                        )

                                    else ->
                                        applyMaterialToPolygon(
                                            polygonPoints = uiState.polygonPoints,
                                            materialBitmap = selectedMaterialBmp
                                        )
                                }
                            }

                            isEditingCurrentOverlay && uiState.currentOverlay!!.holePoints.isNotEmpty() ->
                                getClippedMaterial(
                                    outerPolygon = uiState.polygonPoints,
                                    materialBitmap = selectedMaterialBmp,
                                    holes = uiState.currentOverlay.holePoints
                                )

                            else ->
                                getClippedMaterial(
                                    outerPolygon = uiState.polygonPoints,
                                    materialBitmap = selectedMaterialBmp
                                )
                        }
                    }

                    val newOverlay = OverlayMaterialModel(
                        overlay = appliedMaterialBitmap.await(),
                        polygonPoints = uiState.polygonPoints,
                        polygonCenter = polygonCenter.await(),
                        material = uiState.selectedMaterial,
                        holePoints = if (isEditingCurrentOverlay) uiState.currentOverlay!!.holePoints else emptyList(),
                        hasPerspective = isPerspectiveEnabled.value,
                        offset = offsetOfOverlay.await()
                    )

                    if (isEditingCurrentOverlay) {
                        uiState.overlays.indexOf(uiState.currentOverlay).takeIf { it != -1 }
                            ?.let { index ->
                                mainUiState.update { state ->
                                    state.copy(
                                        overlays = state.overlays.toPersistentList()
                                            .set(index, newOverlay),
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

    private fun applyMaterialWithHole(
        editingOverlays: Map<Int, OverlayMaterialModel>,
        holePoints: List<Offset>
    ) {
        viewModelScope.launch {
            if (!holePoints.canMakeClosedShape()) return@launch // Early exit if hole is invalid

            loadingApplyMaterialState.value = true
            val overlays = mainUiState.value.overlays

            val updatedOverlays = editingOverlays.mapNotNull { (key, editingOverlay) ->
                async(Dispatchers.IO) {
                    val selectedMaterialBmp = editingOverlay.material.let { material ->
                        mainRepository.getBitmap(material)
                            .filter { it.isSuccess }
                            .mapNotNull { it.getOrNull() }
                            .firstOrNull()
                    }

                    if (selectedMaterialBmp == null) {
                        timberE("No valid bitmap could be fetched for overlay $key.")
                        return@async null
                    }

                    val newHolePoints = editingOverlay.holePoints.toSet().plus(listOf(holePoints))

                    val appliedMaterialBitmap = if (editingOverlay.hasPerspective) {
                        applyMaterialToPolygonWithHoles(
                            outerPolygon = editingOverlay.polygonPoints,
                            materialBitmap = selectedMaterialBmp,
                            holes = newHolePoints.toList()
                        )
                    } else {
                        getClippedMaterial(
                            outerPolygon = editingOverlay.polygonPoints,
                            materialBitmap = selectedMaterialBmp,
                            holes = newHolePoints.toList()
                        )
                    }

                    editingOverlay.copy(
                        overlay = appliedMaterialBitmap,
                        holePoints = newHolePoints.toList()
                    ) to key
                }
            }.awaitAll()

            // If any changes were made, apply them in one atomic update
            if (updatedOverlays.isNotEmpty()) {
                mainUiState.update { state ->
                    state.copy(
                        overlays = overlays.mapIndexed { index, overlay ->
                            updatedOverlays.find { it?.second == index }?.first ?: overlay
                        },
                        polygonPoints = emptyList()
                    )
                }
            }
        }
    }

    private fun allOverlaysDrawn() {
        viewModelScope.launch {
            timber("allOverlaysDrawn", "allOverlaysDrawn called")
            loadingApplyMaterialState.value = false
        }
    }

    private fun insertPolygonPoint(offset: Offset) {
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

    private fun updatePolygonPoint(index: Int, offset: Offset) {
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

    private fun extendPolygonPoints(offset: Offset) {
        viewModelScope.launch {
            val points = mainUiState.value.polygonPoints

            if (points.canMakeClosedShape()) {
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
                        overlays = state.currentOverlay?.let { state.overlays - it }
                            ?: state.overlays,
                        currentOverlay = null,
                        polygonPoints = state.polygonPoints.toPersistentList()
                            .add(nextIndex, offset)
                    )
                }
            } else {
                // Add the point normally for the initial shape creation
                insertPolygonPoint(offset)
            }
        }
    }

    private fun memorizeUpdatedPolygonPoints() {
        viewModelScope.launch {
            isRecentActionSavingEnabled = true
            mainUiState.update { it.copy(trigger = !it.trigger) }
        }
    }

    private fun updateCurrentOverlayTransform(
        zoomChange: Float,
        offsetChange: Offset,
        rotationChange: Float
    ) {
        viewModelScope.launch {
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch
            val overlays = mainUiState.value.overlays.toPersistentList()
            val newPolygonPoints = async { currentOverlay.polygonPoints.map { it + offsetChange } }
            val overlayIndex = overlays.indexOf(currentOverlay)
            val updatedOverlay = currentOverlay.copy(
                polygonPoints = newPolygonPoints.await(),
                scale = currentOverlay.scale * zoomChange,
                rotation = currentOverlay.rotation + rotationChange,
                offset = currentOverlay.offset + offsetChange,
            )
            mainUiState.update {
                it.copy(
                    overlays = overlays.set(overlayIndex, updatedOverlay),
                    currentOverlay = updatedOverlay,
                    polygonPoints = newPolygonPoints.await()
                )
            }
        }
    }

    private fun updateCurrentOverlayPosition(overlayIndex: Int?, dragAmount: Offset) {
        var index = overlayIndex
        if (index == null) {
            index = mainUiState.value.overlays.indexOf(mainUiState.value.currentOverlay)
        } else if (overlayIndex !in mainUiState.value.overlays.indices) return

        viewModelScope.launch {
            isRecentActionSavingEnabled = false
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch
            val newPolygonPoints = async { currentOverlay.polygonPoints.map { it + dragAmount } }
            val newPosition = async { currentOverlay.offset + dragAmount }
            val updatedOverlay = currentOverlay.copy(
                polygonPoints = newPolygonPoints.await(),
                offset = newPosition.await()
            )

            timber("PIP_CHECK", "updatedOverlay=$updatedOverlay")
            mainUiState.update { state ->
                state.copy(
                    overlays = state.overlays.toPersistentList().set(index, updatedOverlay),
                    currentOverlay = updatedOverlay,
                    polygonPoints = newPolygonPoints.await()
                )
            }
        }
    }

    private fun clearPolygonPoints() {
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
                timber(
                    "RecentActionsLog",
                    "reAct condition=${(mainUiState.value != recentAction.mainUiState)}"
                )
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

    private fun bringHistoryWork(workModel: WorkModel) {
//        saveCurrentWork()
        mainUiState.update {
            it.copy(
                imageBmp = workModel.baseImage.asImageBitmap(),
                overlays = workModel.overlays
            )
        }
    }
}
