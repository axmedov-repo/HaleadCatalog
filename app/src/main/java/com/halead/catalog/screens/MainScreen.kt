package com.halead.catalog.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.components.FunctionsMenu
import com.halead.catalog.components.ImageSelector
import com.halead.catalog.components.MaterialsMenu
import com.halead.catalog.data.enums.FunctionsEnum

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    var showImagePickerDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        FunctionsMenu(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            canUndo = mainUiState.canUndo,
            canRedo = mainUiState.canRedo,
            selectedCursor = mainUiState.currentCursor,
            onFunctionClicked = { function ->
                if (function.type == FunctionsEnum.REPLACE_IMAGE) {
                    showImagePickerDialog = true
                } else {
                    mainViewModel.selectFunction(function)
                }
            },
            onCursorClicked = { mainViewModel.selectCursor(it) }
        )

        ImageSelector(
            modifier = Modifier
                .fillMaxHeight()
                .weight(8f),
            materials = mainUiState.materials,
            imageBmp = mainUiState.imageBmp,
            showImagePicker = showImagePickerDialog,
            selectedMaterial = mainUiState.selectedMaterial,
            overlays = mainUiState.overlays,
            changeImagePickerVisibility = { showImagePickerDialog = it },
            onOverlayAdded = { mainViewModel.addOverlay(it) }
        ) {
            mainViewModel.selectImage(it)
        }

        MaterialsMenu(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f).background(Color.Red),
            materials = mainUiState.materials,
            selectedMaterial = mainUiState.selectedMaterial,
            onMaterialSelected = { mainViewModel.selectMaterial(it) }
        )
    }
}
