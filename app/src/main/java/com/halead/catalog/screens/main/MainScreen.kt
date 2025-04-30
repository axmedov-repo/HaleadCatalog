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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.R
import com.halead.catalog.components.FunctionsMenu
import com.halead.catalog.components.ImageSelector
import com.halead.catalog.components.MaterialsMenu
import com.halead.catalog.components.PerspectiveSwitch
import com.halead.catalog.components.PrimaryButton
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.enums.ImageSelectingPurpose
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.timber

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel<MainViewModelImpl>()

    val uiState by viewModel.mainUiState.collectAsState()
    val loadingApplyMaterial by viewModel.loadingApplyMaterialState.collectAsState()
    val currentCursor by viewModel.currentCursorState.collectAsState()
    val perspectiveSwitchValue by viewModel.isPerspectiveEnabled.collectAsState()
    val isEditorFullScreen by viewModel.isEditorFullScreen.collectAsState()
    val editorSize by viewModel.editorSize.collectAsState()

    MainScreenContent(
        modifier = modifier,
        uiState = uiState,
        loadingApplyMaterial = loadingApplyMaterial,
        currentCursor = currentCursor,
        perspectiveSwitchValue = perspectiveSwitchValue,
        isEditorInFullScreen = isEditorFullScreen,
        editorSize = editorSize,
        onUiEvent = viewModel::onUiEvent
    )
}

@Composable
private fun MainScreenContent(
    modifier: Modifier,
    uiState: MainUiState,
    loadingApplyMaterial: Boolean,
    currentCursor: CursorData,
    perspectiveSwitchValue: Boolean,
    isEditorInFullScreen: Boolean,
    editorSize: IntSize,
    onUiEvent: (MainUiEvent) -> Unit
) {
    var imageSelectingPurpose by remember { mutableStateOf(ImageSelectingPurpose.EDITING_IMAGE) }
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
//    var isEditorInFullScreen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isPrimaryButtonEnabled by remember(
        uiState.selectedMaterial,
        uiState.imageBmp,
        uiState.polygonPoints
    ) {
        derivedStateOf {
            uiState.selectedMaterial != null && uiState.imageBmp != null && uiState.polygonPoints.canMakeClosedShape()
        }
    }
    val primaryButtonText by remember(
        uiState.materials,
        uiState.selectedMaterial,
        uiState.polygonPoints,
        uiState.isMaterialApplied
    ) {
        derivedStateOf {
            context.getString(
                when {
                    uiState.materials.isEmpty() -> R.string.loading_materials
                    uiState.selectedMaterial == null -> R.string.select_material
                    !uiState.polygonPoints.canMakeClosedShape() -> R.string.select_region
                    uiState.isMaterialApplied -> R.string.material_applied
                    else -> R.string.apply_material
                }
            )
        }
    }

    val primaryButtonContainerColor by remember(
        uiState.isMaterialApplied,
        uiState.polygonPoints.size
    ) {
        derivedStateOf {
            if (uiState.isMaterialApplied && uiState.polygonPoints.canMakeClosedShape()) SelectedItemColor else ButtonColor
        }
    }

    val primaryButton: @Composable (Modifier) -> Unit = remember(
        primaryButtonText,
        isPrimaryButtonEnabled,
        uiState.materials.isEmpty(),
        uiState.selectedMaterial != null,
        primaryButtonContainerColor
    ) {
        @Composable { modifier ->
            PrimaryButton(
                modifier = modifier,
                primaryButtonText = primaryButtonText,
                isPrimaryButtonEnabled = isPrimaryButtonEnabled,
                isMaterialsEmpty = uiState.materials.isEmpty(),
                containerColor = primaryButtonContainerColor,
                onClick = { onUiEvent(MainUiEvent.ApplyMaterial) }
            )
        }
    }

    val materialsMenuContent: @Composable (Modifier) -> Unit = remember(
        uiState.materials,
        uiState.selectedMaterial,
        loadingApplyMaterial
    ) {
        @Composable { modifier ->
            MaterialsMenu(
                modifier = modifier,
                materials = uiState.materials,
                selectedMaterial = uiState.selectedMaterial,
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
        uiState.canUndo,
        uiState.canRedo,
        uiState.imageBmp,
        uiState.overlays,
        uiState.polygonPoints.size,
        uiState.currentOverlay,
        currentCursor
    ) {
        @Composable { modifier ->
            FunctionsMenu(
                modifier = modifier,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                baseImage = uiState.imageBmp,
                overlays = uiState.overlays,
                polygonPointsSize = uiState.polygonPoints.size,
                selectedOverlay = uiState.currentOverlay,
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
        uiState.imageBmp,
        imageSelectingPurpose,
        showImagePickerDialog,
        uiState.polygonPoints,
        uiState.overlays,
        editorSize,
        currentCursor,
    ) {
        @Composable { modifier ->
            Box(modifier = modifier) {
                ImageSelector(
                    imageBmp = uiState.imageBmp,
                    purpose = imageSelectingPurpose,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = uiState.polygonPoints,
                    overlays = uiState.overlays,
                    currentCursor = currentCursor,
                    editorSize = editorSize,
                    changeImagePickerVisibility = { value ->
                        imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                        showImagePickerDialog = value
                    },
                    onMainUiEvent = onUiEvent
                )

                if (uiState.editorHasImage) {
                    FullScreenIcon(
                        isEditorInFullScreen,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        onUiEvent(MainUiEvent.ChangeEditorScreenSize)
                    }
                }
            }
        }
    }

    Row(
        modifier
            .fillMaxSize()
            .padding(8.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .weight(10f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isEditorInFullScreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    functionsMenu(
                        Modifier
                            .weight(1f)
                            .wrapContentHeight()
                    )
                    perspectiveSwitch()
                }
            }
            imageSelector(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = if (isEditorInFullScreen) 8.dp else 0.dp)
            )
        }

        if (!isEditorInFullScreen) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                primaryButton(Modifier.fillMaxWidth())
                materialsMenuContent(
                    Modifier
                        .weight(2f)
                        .fillMaxHeight()
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
    Icon(
        imageVector =
            ImageVector.vectorResource(
                if (isEditorInFullScreen)
                    R.drawable.ic_close_fullscreen
                else
                    R.drawable.ic_open_in_full
            ),
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
            .drawBehind {
                drawCircle(
                    if (isEditorInFullScreen)
                        SelectedItemColor
                    else
                        Color.Gray.copy(0.1f)
                )
            }
            .padding(8.dp)
            .size(16.dp),
        tint = if (isEditorInFullScreen) Color.White else Color.Gray,
        contentDescription = null
    )
}
