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
import com.halead.catalog.components.TopBarFunctionType
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.screens.work.WorkHistoryPanel
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor

// TODO: Use Layout Inspector to optimize recompositions
@Composable
fun MainScreen(
    selectedTopBarFunction: TopBarFunctionType? = null,
    viewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val mainUiState by viewModel.mainUiState.collectAsState()
    val loadingApplyMaterialState by viewModel.loadingApplyMaterialState.collectAsState()
    val currentCursorState by viewModel.currentCursorState.collectAsState()
    val perspectiveSwitchValue by viewModel.switchValue.collectAsState()
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }

    val isPrimaryButtonEnabled by remember(mainUiState.selectedMaterial, mainUiState.imageBmp) {
        derivedStateOf {
            mainUiState.selectedMaterial != null && mainUiState.imageBmp != null
        }
    }
    val primaryButtonText by remember(
        mainUiState.materials,
        mainUiState.selectedMaterial,
        mainUiState.polygonPoints,
        mainUiState.isMaterialApplied
    ) {
        derivedStateOf {
            when {
                mainUiState.materials.isEmpty() -> "Loading Materials"
                mainUiState.selectedMaterial == null -> "Select Material"
                mainUiState.polygonPoints.size < 3 -> "Draw Region"
                mainUiState.isMaterialApplied -> "Material Applied"
                else -> "Apply Material"
            }
        }
    }
    val primaryButtonContainerColor by remember(mainUiState.isMaterialApplied, mainUiState.polygonPoints.size) {
        derivedStateOf {
            if (mainUiState.isMaterialApplied && mainUiState.polygonPoints.size >= 3) SelectedItemColor else ButtonColor
        }
    }
    val onPrimaryButtonClick = remember { { viewModel.applyMaterial() } }

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
                        canUndo = mainUiState.canUndo,
                        canRedo = mainUiState.canRedo,
                        baseImage = mainUiState.imageBmp,
                        overlays = mainUiState.overlays,
                        polygonPointsSize = mainUiState.polygonPoints.size,
                        selectedOverlay = mainUiState.currentOverlay,
                        selectedCursor = currentCursorState,
                        onFunctionClicked = { function ->
                            if (function.type == FunctionsEnum.RESET_IMAGE) {
                                showImagePickerDialog = true
                            } else {
                                viewModel.selectFunction(function)
                            }
                        },
                        onCursorClicked = { cursor ->
                            viewModel.selectCursor(cursor)
                        }
                    )
                    PerspectiveSwitch(
                        isChecked = perspectiveSwitchValue,
                        onCheckedChange = viewModel::changeSwitchValue
                    )
                }
                ImageSelector(
                    modifier = Modifier.weight(1f),
                    imageBmp = mainUiState.imageBmp,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = mainUiState.polygonPoints,
                    overlays = mainUiState.overlays,
                    currentCursor = currentCursorState,
                    changeImagePickerVisibility = { showImagePickerDialog = it }
                ) {
                    viewModel.selectImage(it)
                }
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
                    isMaterialsEmpty = mainUiState.materials.isEmpty(),
                    isMaterialSelected = mainUiState.selectedMaterial != null,
                    containerColor = primaryButtonContainerColor,
                    onClick = onPrimaryButtonClick
                )
                MaterialsMenu(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color.Red),
                    materials = mainUiState.materials,
                    selectedMaterial = mainUiState.selectedMaterial,
                    loadingApplyMaterial = loadingApplyMaterialState,
                    onMaterialSelected = { viewModel.selectMaterial(it) }
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            when (selectedTopBarFunction) {
                TopBarFunctionType.MENU -> {}
                TopBarFunctionType.HISTORY -> {
                    WorkHistoryPanel(
                        onWorkClick = { viewModel.bringHistoryWork(it) }
                    )
                }

                TopBarFunctionType.SHARE -> {}
                TopBarFunctionType.SETTINGS -> {}
                null -> {}
            }
        }
    }
}
