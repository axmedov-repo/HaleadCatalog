package com.halead.catalog.screens.main

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halead.catalog.data.RecentAction
import com.halead.catalog.data.RecentActions
import com.halead.catalog.data.Settings
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.DefaultCursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.models.OverlayData
import com.halead.catalog.data.models.OverlayMaterial
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
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.getTemporaryClippedOverlay
import com.halead.catalog.utils.isQuadrilateral
import com.halead.catalog.utils.rotatePoints
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.timberE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorScreenState(
    val isFullScreen: Boolean = false,
    val size: IntSize = IntSize.Zero
)

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private var isRecentActionSavingEnabled = true
    private var isUndoEnabled: Boolean = true
    private var isRedoEnabled: Boolean = true

    override val editorScreenState = MutableStateFlow(EditorScreenState())
    private var widthDiffFullScreen: Int = 0
    private var heightDiffFullScreen: Int = 0
    private var fullScreenEditorSize: IntSize = IntSize.Zero
    private var normalEditorSize: IntSize = IntSize.Zero

    private var hasWidthDiffMeasured = false
    private var hasHeightDiffMeasured = false
    private var hasEditorSizeMeasured = false
    private var hasEditorFullSizeMeasured = false

    init {
        viewModelScope.launch {
            getMaterials()
            launch { observeIsMaterialApplied() }
            launch { observeToSaveCurrentState() }
            launch { observePerspectiveChange() }
        }
    }

    override fun onUiEvent(mainUiEvent: MainUiEvent) {
        viewModelScope.launch {
            when (mainUiEvent) {
                is MainUiEvent.AddMaterial -> addMaterial(bitmap = mainUiEvent.bitmap)
                MainUiEvent.AllOverlaysDrawn -> allOverlaysDrawn()
                MainUiEvent.ApplyMaterial -> applyMaterial()
                is MainUiEvent.BringHistoryWork -> bringHistoryWork(workModel = mainUiEvent.workModel)
                is MainUiEvent.ChangePerspective -> changePerspective(value = mainUiEvent.value)
                MainUiEvent.ClearPolygonPoints -> clearPolygonPoints()
                is MainUiEvent.ExtendPolygonPoints -> extendPolygonPoints(offset = mainUiEvent.offset)
                is MainUiEvent.InsertPolygonPoint -> insertPolygonPoint(offset = mainUiEvent.offset)
                MainUiEvent.SaveCurrentState -> saveCurrentState()
                is MainUiEvent.SelectCursor -> selectCursor(cursorData = mainUiEvent.cursorData)
                is MainUiEvent.SelectFunction -> selectFunction(function = mainUiEvent.function)
                is MainUiEvent.SelectImage -> selectImage(bitmap = mainUiEvent.bitmap)
                MainUiEvent.ResetCurrentImage -> resetImage()
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

                is MainUiEvent.SaveHeightDiffOfFullScreen -> saveHeightDiffOfFullScreen(height = mainUiEvent.height)
                is MainUiEvent.SaveWidthDiffOfFullScreen -> saveWidthDiffOfFullScreen(width = mainUiEvent.width)
                is MainUiEvent.SaveEditorSize -> saveEditorSize(size = mainUiEvent.size)
                is MainUiEvent.SaveEditorFullSize -> saveEditorFullSize(size = mainUiEvent.size)
                MainUiEvent.OnFullScreenChanged -> onFullScreenChanged()
            }
        }
    }

    private fun saveHeightDiffOfFullScreen(height: Int) {
        if (!hasHeightDiffMeasured) {
            heightDiffFullScreen = height
            timber("FullScreenIcon", "ViewModel:MainUiEvent.HeightDiff=$heightDiffFullScreen")
            hasHeightDiffMeasured = true
        }
    }

    private fun saveWidthDiffOfFullScreen(width: Int) {
        if (!hasWidthDiffMeasured) {
            widthDiffFullScreen = width
            timber("FullScreenIcon", "ViewModel:MainUiEvent.WidthDiff=$widthDiffFullScreen")
            hasWidthDiffMeasured = true
        }
    }

    private fun saveEditorSize(size: IntSize) {
        if (!hasEditorSizeMeasured) {
            size.takeIf { it != normalEditorSize }?.let {
                normalEditorSize = it
                timber("FullScreenIcon", "ViewModel:MainUiEvent.EditorSize=$normalEditorSize")

                editorScreenState.update { current ->
                    current.copy(
                        size = if (current.isFullScreen) fullScreenEditorSize else normalEditorSize
                    )
                }
            }
            hasEditorSizeMeasured = true
        }
    }

    private suspend fun saveEditorFullSize(size: IntSize) = coroutineScope {
        if (!hasEditorFullSizeMeasured) {
            size.let { fullSize ->
                hasEditorFullSizeMeasured = true
                mainUiState
                    .map { it.imageBmp }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect { bmp ->
                        val aspectRatio = bmp.width.toFloat() / bmp.height.toFloat()
                        val preSize = calculateChildSize(
                            fullSize.width - dpToPx(context, 32f),
                            fullSize.height - dpToPx(context, 16f),
                            aspectRatio
                        )

                        fullScreenEditorSize = preSize
                        timber(
                            "FullScreenIcon",
                            "ViewModel:MainUiEvent.FullscreenSize=$fullScreenEditorSize}"
                        )

                        editorScreenState.update { current ->
                            current.copy(
                                size = if (current.isFullScreen) fullScreenEditorSize else normalEditorSize
                            )
                        }
                    }
            }
        }
    }

    private fun calculateChildSize(containerWidth: Int, containerHeight: Int, aspectRatio: Float): IntSize {
        // First try matching width
        val widthMatchHeight = containerWidth / aspectRatio
        return if (widthMatchHeight <= containerHeight) {
            IntSize(containerWidth, widthMatchHeight.toInt())
        } else {
            // Fallback: match height
            val height = containerHeight
            val width = height * aspectRatio
            IntSize(width.toInt(), height)
        }
    }

    private fun onFullScreenChanged() {
        timber("FullScreenIcon", "ViewModel:MainUiEvent.OnFullScreenChanged")
        editorScreenState.update { current ->
            val newIsFullScreen = !current.isFullScreen
            current.copy(
                isFullScreen = newIsFullScreen,
                size = if (newIsFullScreen) fullScreenEditorSize else normalEditorSize
            )
        }
    }

    private suspend fun changePerspective(value: Boolean) = coroutineScope {
        settings.changePerspective(value)
    }

    private suspend fun observePerspectiveChange() {
        isPerspectiveEnabled.collect {
            if (mainUiState.value.currentOverlay != null) {
                mainUiState.update { uiState ->
                    uiState.copy(
                        isMaterialApplied = mainUiState.value.currentOverlay?.hasPerspective == it
                    )
                }
            }
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
        return if (overlayPoints.isEmpty() || (mainUiState.value.polygonPoints.minus(overlayPoints)
                .isNotEmpty())
        ) {
            false
        } else {
            mainUiState.value.currentOverlay?.material?.resId == mainUiState.value.selectedMaterial?.resId &&
                    mainUiState.value.currentOverlay?.hasPerspective == isPerspectiveEnabled.value
        }
    }

    private suspend fun observeToSaveCurrentState() {
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

    private suspend fun getMaterials() = coroutineScope {
        mainRepository.getMaterials().onEach { response ->
            response.onSuccess { result ->
                mainUiState.update {
                    it.copy(materials = result.toImmutableList())
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
        val materialBmpFromRes by lazy { getBitmapFromResource(context, material) }

        viewModelScope.launch {
            (mainRepository.getBitmap(material) ?: materialBmpFromRes)?.asImageBitmap()?.let { materialBmp ->
                mainUiState.update {
                    it.copy(
                        selectedMaterial = OverlayMaterial(
                            resId = material,
                            bitmap = materialBmp
                        )
                    )
                }
            }
        }
    }

    private fun selectImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            mainUiState.update {
                it.copy(
                    imageBmp = bitmap?.asImageBitmap(),
                    overlays = persistentListOf(),
                    polygonPoints = persistentListOf(),
                    currentOverlay = null
                )
            }
            currentCursorState.update { DefaultCursorData }
        }
    }

    private fun resetImage() {
        mainUiState.update {
            it.copy(
                overlays = persistentListOf(),
                polygonPoints = persistentListOf(),
                currentOverlay = null
            )
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
                            polygonPoints = persistentListOf(),
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
                                persistentListOf()
                            } else {
                                it.overlays.minus(it.currentOverlay!!)
                            }.toImmutableList(),
                            currentOverlay = null,
                            polygonPoints = persistentListOf()
                        )
                    }
                }

                FunctionsEnum.REMOVE_SELECTION -> {
                    mainUiState.value.polygonPoints.takeIf { it.isNotEmpty() }
                        ?.let { polygonToRemove ->
                            val editingOverlays = HashMap<Int, OverlayData>()

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
                polygonPoints = newPolygonPoints.await().toImmutableList()
                // Don't update the offset here
            )

            mainUiState.update {
                it.copy(
                    overlays = it.overlays.toPersistentList()
                        .set(it.overlays.indexOf(currentOverlay), newOverlay),
                    currentOverlay = newOverlay,
                    polygonPoints = newPolygonPoints.await().toImmutableList()
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

    private fun selectOverlay(overlay: OverlayData) {
        viewModelScope.launch {
            if (needToKeepCurrentPolygons()) {
                keepCurrentPolygonPointsWithoutMaterial()
            }
            timber("PIP_CHECK", "$overlay")
            mainUiState.update {
                it.copy(
                    polygonPoints = overlay.polygonPoints.toImmutableList(),
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
                    polygonPoints = persistentListOf(),
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

        val newOverlay = OverlayData(
            overlay = appliedMaterialBitmap.await().asImageBitmap(),
            polygonPoints = uiState.polygonPoints,
            polygonCenter = polygonCenter.await(),
            material = null,
            hasPerspective = isPerspectiveEnabled.value,
            offset = offsetOfOverlay.await()
        )

        mainUiState.update {
            it.copy(
                overlays = it.overlays.plus(newOverlay).toImmutableList()
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

                val selectedMaterialBmp = uiState.selectedMaterial?.bitmap?.asAndroidBitmap()
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
                                            holes = uiState.currentOverlay!!.holePoints,
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
                                    holes = uiState.currentOverlay!!.holePoints
                                )

                            else ->
                                getClippedMaterial(
                                    outerPolygon = uiState.polygonPoints,
                                    materialBitmap = selectedMaterialBmp
                                )
                        }
                    }

                    val newOverlay = OverlayData(
                        overlay = appliedMaterialBitmap.await().asImageBitmap(),
                        polygonPoints = uiState.polygonPoints,
                        polygonCenter = polygonCenter.await(),
                        material = uiState.selectedMaterial,
                        holePoints = if (isEditingCurrentOverlay) uiState.currentOverlay!!.holePoints else persistentListOf(),
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
                                overlays = it.overlays.plus(newOverlay).toImmutableList(),
                                currentOverlay = newOverlay
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyMaterialWithHole(
        editingOverlays: Map<Int, OverlayData>,
        holePoints: List<Offset>
    ) {
        viewModelScope.launch {
            if (!holePoints.canMakeClosedShape()) return@launch // Early exit if hole is invalid

            loadingApplyMaterialState.value = true
            val overlays = mainUiState.value.overlays

            val updatedOverlays = editingOverlays.mapNotNull { (key, editingOverlay) ->
                async(Dispatchers.IO) {
                    val selectedMaterialBmp = editingOverlay.material?.bitmap?.asAndroidBitmap()

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
                        overlay = appliedMaterialBitmap.asImageBitmap(),
                        holePoints = newHolePoints.toImmutableList()
                    ) to key
                }
            }.awaitAll()

            // If any changes were made, apply them in one atomic update
            if (updatedOverlays.isNotEmpty()) {
                mainUiState.update { state ->
                    state.copy(
                        overlays = overlays.mapIndexed { index, overlay ->
                            updatedOverlays.find { it?.second == index }?.first ?: overlay
                        }.toImmutableList(),
                        polygonPoints = persistentListOf()
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
                    overlays = (state.currentOverlay?.let { state.overlays - it } ?: state.overlays).toImmutableList(),
                    currentOverlay = null,
                    polygonPoints = state.polygonPoints.plus(offset).toImmutableList()
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
                    overlays = (state.currentOverlay?.let { state.overlays - it } ?: state.overlays).toImmutableList(),
                    currentOverlay = null,
                    polygonPoints = state.polygonPoints.toPersistentList().set(index, offset)
                )
            }
        }
    }

    private fun extendPolygonPoints(offset: Offset) {
        viewModelScope.launch {
            val points = mainUiState.value.polygonPoints

            val canMakeClosedShape = points.canMakeClosedShape()
            timber("PolygonPoints", "canMakeClosedShape=$canMakeClosedShape")

            if (canMakeClosedShape) {
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
                        overlays = (state.currentOverlay?.let { state.overlays - it }
                            ?: state.overlays).toImmutableList(),
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

    private fun saveCurrentState() {
        viewModelScope.launch {
            timber("RecentActionsLog", "Saved Current State")
            delay(100)  // to avoid state updating race conditions
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
            isRecentActionSavingEnabled = false
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch
            val overlays = mainUiState.value.overlays.toPersistentList()
            val overlayIndex = overlays.indexOf(currentOverlay)
            val center = currentOverlay.polygonCenter

            fun transform(point: Offset): Offset {
                val relative = point - center

                // Apply scaling
                val scaled = Offset(relative.x * zoomChange, relative.y * zoomChange)

                // Apply rotation
                val angleRad = Math.toRadians(rotationChange.toDouble())
                val rotated = Offset(
                    (scaled.x * Math.cos(angleRad) - scaled.y * Math.sin(angleRad)).toFloat(),
                    (scaled.x * Math.sin(angleRad) + scaled.y * Math.cos(angleRad)).toFloat()
                )

                // Apply translation
                return rotated + center + offsetChange
            }

            val newPolygonPoints = currentOverlay.polygonPoints.map(::transform).toImmutableList()

            val updatedOverlay = currentOverlay.copy(
                polygonPoints = newPolygonPoints,
                scale = currentOverlay.scale * zoomChange,
                rotation = currentOverlay.rotation + rotationChange,
                offset = currentOverlay.offset + offsetChange,
            )

            mainUiState.update {
                it.copy(
                    overlays = overlays.set(overlayIndex, updatedOverlay),
                    currentOverlay = updatedOverlay,
                    polygonPoints = newPolygonPoints
                )
            }
        }
    }

    private fun updateCurrentOverlayPosition(overlayIndex: Int?, dragAmount: Offset) {
        var index = overlayIndex
        if (index == null) {
            index = mainUiState.value.overlays.indexOf(mainUiState.value.currentOverlay)
        }
        if (index !in mainUiState.value.overlays.indices) return

        viewModelScope.launch {
            isRecentActionSavingEnabled = false
            val currentOverlay = mainUiState.value.currentOverlay ?: return@launch
            val newPolygonPoints = async { currentOverlay.polygonPoints.map { it + dragAmount } }
            val newPosition = async { currentOverlay.offset + dragAmount }
            val updatedOverlay = currentOverlay.copy(
                polygonPoints = newPolygonPoints.await().toImmutableList(),
                offset = newPosition.await()
            )

            timber("PIP_CHECK", "updatedOverlay=$updatedOverlay")
            mainUiState.update { state ->
                state.copy(
                    overlays = state.overlays.toPersistentList().set(index, updatedOverlay),
                    currentOverlay = updatedOverlay,
                    polygonPoints = newPolygonPoints.await().toImmutableList()
                )
            }
        }
    }

    private fun clearPolygonPoints() {
        viewModelScope.launch {
            mainUiState.update {
                it.copy(polygonPoints = persistentListOf())
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
                overlays = workModel.overlays.toImmutableList()
            )
        }
    }
}
