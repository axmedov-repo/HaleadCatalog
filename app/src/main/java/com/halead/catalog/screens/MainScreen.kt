package com.halead.catalog.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> mainViewModel.selectImage(uri) }

    Row(
        modifier = Modifier
            .fillMaxSize(),
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
                    launcher.launch("image/*")
                } else {
                    mainViewModel.selectFunction(function)
                }
            },
            onCursorClicked = { cursor ->
                mainViewModel.selectCursor(cursor)
            }
        )

        ImageSelector(
            modifier = Modifier
                .fillMaxHeight()
                .weight(8f),
            materials = mainUiState.materials,
            imageBmp = mainUiState.imageBmp,
            selectedMaterial = mainUiState.selectedMaterial,
            overlays = mainUiState.overlays,
            onOverlayAdded = { mainViewModel.addOverlay(it) }
        ) {
            launcher.launch("image/*")
        }

        MaterialsMenu(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            materials = mainUiState.materials,
            selectedMaterial = mainUiState.selectedMaterial,
            onMaterialSelected = { material -> mainViewModel.selectMaterial(material) }
        )
    }
}
