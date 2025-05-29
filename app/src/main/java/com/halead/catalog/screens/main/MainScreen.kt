package com.halead.catalog.screens.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.R
import com.halead.catalog.components.CursorsMenu
import com.halead.catalog.components.FunctionsMenu
import com.halead.catalog.components.ImageSelector
import com.halead.catalog.components.MaterialsMenu
import com.halead.catalog.components.PerspectiveSwitch
import com.halead.catalog.components.PrimaryButton
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.enums.ImageSelectingPurpose
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.timber

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel<MainViewModelImpl>()

    val uiState = viewModel.mainUiState.collectAsState().value
    val loadingApplyMaterial by viewModel.loadingApplyMaterialState.collectAsState()
    val currentCursor by viewModel.currentCursorState.collectAsState()
    val perspectiveSwitchValue by viewModel.isPerspectiveEnabled.collectAsState()
    val editorScreenState by viewModel.editorScreenState.collectAsState()

    LaunchedEffect(editorScreenState.isFullScreen) {
        timber("FullScreenIcon", "editorScreenState=$editorScreenState")
    }

    MainScreenContent(
        modifier = modifier,
        imageBmp = uiState.imageBmp,
        selectedMaterial = uiState.selectedMaterial,
        materials = uiState.materials,
        overlays = uiState.overlays,
        polygonPoints = uiState.polygonPoints,
        currentOverlay = uiState.currentOverlay,
        isMaterialApplied = uiState.isMaterialApplied,
        canUndo = uiState.canUndo,
        canRedo = uiState.canRedo,
        editorHasImage = uiState.editorHasImage,
        loadingApplyMaterial = loadingApplyMaterial,
        currentCursor = currentCursor,
        perspectiveSwitchValue = perspectiveSwitchValue,
        editorScreenState = editorScreenState,
        onUiEvent = viewModel::onUiEvent
    )
}

@Composable
private fun MainScreenContent(
    modifier: Modifier,
    imageBmp: ImageBitmap?,
    selectedMaterial: Int?,
    materials: List<Int>,
    overlays: List<OverlayMaterialModel>,
    polygonPoints: List<Offset>,
    currentOverlay: OverlayMaterialModel?,
    isMaterialApplied: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    editorHasImage: Boolean,
    loadingApplyMaterial: Boolean,
    currentCursor: CursorData,
    perspectiveSwitchValue: Boolean,
    editorScreenState: EditorScreenState,
    onUiEvent: (MainUiEvent) -> Unit
) {
    var imageSelectingPurpose by remember { mutableStateOf(ImageSelectingPurpose.EDITING_IMAGE) }
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
//    var isEditorInFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isPrimaryButtonEnabled by remember(selectedMaterial, imageBmp, polygonPoints) {
        derivedStateOf {
            selectedMaterial != null && imageBmp != null && polygonPoints.canMakeClosedShape()
        }
    }

    val primaryButtonText by remember(
        materials,
        selectedMaterial,
        polygonPoints,
        isMaterialApplied
    ) {
        derivedStateOf {
            context.getString(
                when {
                    materials.isEmpty() -> R.string.loading_materials
                    selectedMaterial == null -> R.string.select_material
                    !polygonPoints.canMakeClosedShape() -> R.string.select_region
                    isMaterialApplied -> R.string.material_applied
                    else -> R.string.apply_material
                }
            )
        }
    }

    val primaryButtonContainerColor by remember(
        isMaterialApplied,
        polygonPoints.size
    ) {
        derivedStateOf {
            if (isMaterialApplied && polygonPoints.canMakeClosedShape()) SelectedItemColor else ButtonColor
        }
    }

    /*val primaryButton: @Composable (Modifier) -> Unit = remember(
        primaryButtonText,
        isPrimaryButtonEnabled,
        materials.isEmpty(),
        selectedMaterial != null,
        primaryButtonContainerColor
    ) {
        @Composable { modifier ->
            PrimaryButton(
                modifier = modifier,
                primaryButtonText = primaryButtonText,
                isPrimaryButtonEnabled = isPrimaryButtonEnabled,
                isMaterialsEmpty = materials.isEmpty(),
                containerColor = primaryButtonContainerColor,
                onClick = { onUiEvent(MainUiEvent.ApplyMaterial) }
            )
        }
    }

    val materialsMenuContent: @Composable (Modifier) -> Unit = remember(
        materials,
        selectedMaterial,
        loadingApplyMaterial
    ) {
        @Composable { modifier ->
            MaterialsMenu(
                modifier = modifier,
                materials = materials,
                selectedMaterial = selectedMaterial,
                loadingApplyMaterial = loadingApplyMaterial,
                onAddMaterial = {
                    imageSelectingPurpose = ImageSelectingPurpose.ADD_MATERIAL
                    showImagePickerDialog = true
                },
                onMaterialSelected = { selectedMaterial: Int ->
                    onUiEvent(MainUiEvent.SelectMaterial(selectedMaterial))
                }
            )
        }
    }

    val functionsMenu: @Composable (Modifier) -> Unit = remember(
        canUndo,
        canRedo,
        imageBmp,
        overlays,
        polygonPoints.size,
        currentOverlay,
        currentCursor
    ) {
        @Composable { modifier ->
            FunctionsMenu(
                modifier = modifier,
                canUndo = canUndo,
                canRedo = canRedo,
                baseImage = imageBmp,
                overlays = overlays,
                polygonPointsSize = polygonPoints.size,
                selectedOverlay = currentOverlay,
                selectedCursor = currentCursor,
                onFunctionClicked = { function: FunctionData ->
                    if (function.type == FunctionsEnum.RESET_IMAGE) {
                        imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                        showImagePickerDialog = true
                    } else {
                        onUiEvent(MainUiEvent.SelectFunction(function))
                    }
                },
                onCursorClicked = { cursorData: CursorData ->
                    onUiEvent(MainUiEvent.SelectCursor(cursorData))
                }
            )
        }
    }

    val perspectiveSwitch: @Composable () -> Unit = remember(
        perspectiveSwitchValue
    ) {
        @Composable {
            PerspectiveSwitch(
                modifier = Modifier.wrapContentWidth(),
                isChecked = perspectiveSwitchValue,
                onCheckedChange = { value ->
                    onUiEvent(MainUiEvent.ChangePerspective(value))
                }
            )
        }
    }

    val imageSelector: @Composable (Modifier) -> Unit = remember(
        imageBmp,
        imageSelectingPurpose,
        showImagePickerDialog,
        polygonPoints,
        overlays,
        editorScreenState,
        currentCursor,
    ) {
        @Composable { modifier ->
            Box(modifier = modifier) {
                ImageSelector(
                    imageBmp = imageBmp,
                    purpose = imageSelectingPurpose,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = polygonPoints,
                    overlays = overlays,
                    currentCursor = currentCursor,
                    editorSize = editorScreenState.size,
                    changeImagePickerVisibility = { value ->
                        imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                        showImagePickerDialog = value
                    },
                    onMainUiEvent = onUiEvent
                )

                if (editorHasImage) {
                    FullScreenIcon(
                        editorScreenState.isFullScreen,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        timber("FullScreenIcon", "Clicked")
                        onUiEvent(MainUiEvent.ChangeEditorScreenSize)
                    }
                }
            }
        }
    }*/

    val rememberOnFunctionClicked = remember {
        { function: FunctionData ->
            if (function.type == FunctionsEnum.RESET_IMAGE) {
                imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                showImagePickerDialog = true
            } else {
                onUiEvent(MainUiEvent.SelectFunction(function))
            }
        }
    }

    val rememberOnCursorClicked = remember {
        { cursorData: CursorData ->
            onUiEvent(MainUiEvent.SelectCursor(cursorData))
        }
    }

    Row(
        modifier
            .fillMaxSize()
            .padding(8.dp)
            .animateContentSize()
    ) {
        CursorsMenu(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            canUndo = canUndo,
            canRedo = canRedo,
            baseImage = imageBmp,
            overlays = overlays,
            polygonPointsSize = polygonPoints.size,
            selectedOverlay = currentOverlay,
            selectedCursor = currentCursor,
            onFunctionClicked = rememberOnFunctionClicked,
            onCursorClicked = rememberOnCursorClicked
        )
        Column(
            modifier = Modifier
                .weight(10f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!editorScreenState.isFullScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
//                    functionsMenu(
//                        Modifier
//                            .weight(1f)
//                            .wrapContentHeight()
//                    )
                    FunctionsMenu(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        canUndo = canUndo,
                        canRedo = canRedo,
                        baseImage = imageBmp,
                        overlays = overlays,
                        polygonPointsSize = polygonPoints.size,
                        selectedOverlay = currentOverlay,
                        selectedCursor = currentCursor,
                        onFunctionClicked = rememberOnFunctionClicked,
                        onCursorClicked = rememberOnCursorClicked
                    )
                    val rememberOnCheckedChange = remember {
                        { value: Boolean ->
                            onUiEvent(MainUiEvent.ChangePerspective(value))
                        }
                    }
                    PerspectiveSwitch(
                        modifier = Modifier.wrapContentWidth(),
                        isChecked = perspectiveSwitchValue,
                        onCheckedChange = rememberOnCheckedChange
                    )
                }
            }
//            imageSelector(
//                Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//                    .padding(start = 8.dp, end = if (editorScreenState.isFullScreen) 8.dp else 0.dp)
//            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = if (editorScreenState.isFullScreen) 8.dp else 0.dp)
            ) {
                ImageSelector(
                    imageBmp = imageBmp,
                    purpose = imageSelectingPurpose,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = polygonPoints,
                    overlays = overlays,
                    currentCursor = currentCursor,
                    editorSize = editorScreenState.size,
                    changeImagePickerVisibility = { value ->
                        imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                        showImagePickerDialog = value
                    },
                    onMainUiEvent = onUiEvent
                )

                if (editorHasImage) {
                    FullScreenIcon(
                        editorScreenState.isFullScreen,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        timber("FullScreenIcon", "Clicked")
                        onUiEvent(MainUiEvent.ChangeEditorScreenSize)
                    }
                }
            }
        }

        if (!editorScreenState.isFullScreen) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    primaryButtonText = primaryButtonText,
                    isPrimaryButtonEnabled = isPrimaryButtonEnabled,
                    isMaterialsEmpty = materials.isEmpty(),
                    containerColor = primaryButtonContainerColor,
                    onClick = { onUiEvent(MainUiEvent.ApplyMaterial) }
                )
//                materialsMenuContent(
//                    Modifier
//                        .weight(2f)
//                        .fillMaxHeight()
//                )
                MaterialsMenu(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    materials = materials,
                    selectedMaterial = selectedMaterial,
                    loadingApplyMaterial = loadingApplyMaterial,
                    onAddMaterial = {
                        imageSelectingPurpose = ImageSelectingPurpose.ADD_MATERIAL
                        showImagePickerDialog = true
                    },
                    onMaterialSelected = { selectedMaterial: Int ->
                        onUiEvent(MainUiEvent.SelectMaterial(selectedMaterial))
                    }
                )
            }
        }
    }
}

@Composable
fun FullScreenIcon(
    isEditorInFullScreen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val image = remember(isEditorInFullScreen) {
        if (isEditorInFullScreen)
            R.drawable.ic_close_fullscreen
        else
            R.drawable.ic_open_in_full
    }

    Icon(
        imageVector = ImageVector.vectorResource(image),
        modifier = modifier
            .padding(4.dp)
            .clip(CircleShape)
            .drawBehind {
                drawCircle(
                    if (isEditorInFullScreen)
                        SelectedItemColor
                    else
                        Color.Gray.copy(0.2f)
                )
            }
            .clickable(onClick = onClick)
            .padding(8.dp)
            .size(16.dp),
        tint = if (isEditorInFullScreen) Color.White else Color.White,
        contentDescription = null
    )
}
