package com.halead.catalog.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.components.FunctionsMenu
import com.halead.catalog.components.ImageSelector
import com.halead.catalog.components.MaterialsMenu
import com.halead.catalog.components.PerspectiveSwitch
import com.halead.catalog.components.PrimaryButton
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.data.enums.ImageSelectingPurpose
import com.halead.catalog.data.states.MainUiState
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.canMakeClosedShape

@Composable
fun MainScreen() {
    val viewModel: MainViewModel = viewModel<MainViewModelImpl>()
    val state by viewModel.mainUiState.collectAsState()
    val loadingApplyMaterialState by viewModel.loadingApplyMaterialState.collectAsState()
    val currentCursorState by viewModel.currentCursorState.collectAsState()
    val perspectiveSwitchValue by viewModel.isPerspectiveEnabled.collectAsState()

    MainScreenContent(
        uiState = state,
        loadingApplyMaterial = loadingApplyMaterialState,
        currentCursor = currentCursorState,
        perspectiveSwitchValue = perspectiveSwitchValue,
        onUiEvent = viewModel::onUiEvent
    )
}

@Composable
private fun MainScreenContent(
    uiState: MainUiState,
    loadingApplyMaterial: Boolean,
    currentCursor: CursorData,
    perspectiveSwitchValue: Boolean,
    onUiEvent: (MainUiEvent) -> Unit
) {
    var imageSelectingPurpose by remember { mutableStateOf(ImageSelectingPurpose.EDITING_IMAGE) }
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
    val isPrimaryButtonEnabled by remember(uiState.selectedMaterial, uiState.imageBmp) {
        derivedStateOf {
            uiState.selectedMaterial != null && uiState.imageBmp != null
        }
    }
    val primaryButtonText by remember(
        uiState.materials,
        uiState.selectedMaterial,
        uiState.polygonPoints,
        uiState.isMaterialApplied
    ) {
        derivedStateOf {
            when {
                uiState.materials.isEmpty() -> "Loading Materials"
                uiState.selectedMaterial == null -> "Select Material"
                !uiState.polygonPoints.canMakeClosedShape() -> "Select Region"
                uiState.isMaterialApplied -> "Material Applied"
                else -> "Apply Material"
            }
        }
    }
    val primaryButtonContainerColor by remember(uiState.isMaterialApplied, uiState.polygonPoints.size) {
        derivedStateOf {
            if (uiState.isMaterialApplied && uiState.polygonPoints.canMakeClosedShape()) SelectedItemColor else ButtonColor
        }
    }
    val onPrimaryButtonClick = remember { { onUiEvent(MainUiEvent.ApplyMaterial) } }

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(10f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FunctionsMenu(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        canUndo = uiState.canUndo,
                        canRedo = uiState.canRedo,
                        baseImage = uiState.imageBmp,
                        overlays = uiState.overlays,
                        polygonPointsSize = uiState.polygonPoints.size,
                        selectedOverlay = uiState.currentOverlay,
                        selectedCursor = currentCursor,
                        onFunctionClicked = { function ->
                            if (function.type == FunctionsEnum.RESET_IMAGE) {
                                imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                                showImagePickerDialog = true
                            } else {
                                onUiEvent(MainUiEvent.SelectFunction(function))
                            }
                        },
                        onCursorClicked = { onUiEvent(MainUiEvent.SelectCursor(it)) }
                    )

                    PerspectiveSwitch(
                        isChecked = perspectiveSwitchValue,
                        onCheckedChange = { onUiEvent(MainUiEvent.ChangeSwitchValue(it)) }
                    )
                }

                ImageSelector(
                    modifier = Modifier.weight(1f),
                    imageBmp = uiState.imageBmp,
                    purpose = imageSelectingPurpose,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = uiState.polygonPoints,
                    overlays = uiState.overlays,
                    currentCursor = currentCursor,
                    changeImagePickerVisibility = {
                        imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                        showImagePickerDialog = it
                    },
                    onMainUiEvent = onUiEvent
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    primaryButtonText = primaryButtonText,
                    isPrimaryButtonEnabled = isPrimaryButtonEnabled,
                    isMaterialsEmpty = uiState.materials.isEmpty(),
                    isMaterialSelected = uiState.selectedMaterial != null,
                    containerColor = primaryButtonContainerColor,
                    onClick = onPrimaryButtonClick
                )

                MaterialsMenu(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color.Red),
                    materials = uiState.materials,
                    selectedMaterial = uiState.selectedMaterial,
                    loadingApplyMaterial = loadingApplyMaterial,
                    onAddMaterial = {
                        imageSelectingPurpose = ImageSelectingPurpose.ADD_MATERIAL
                        showImagePickerDialog = true
                    },
                    onMaterialSelected = { onUiEvent(MainUiEvent.SelectMaterial(it)) }
                )
            }
        }
    }
}
