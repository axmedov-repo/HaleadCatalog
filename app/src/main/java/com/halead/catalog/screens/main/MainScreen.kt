package com.halead.catalog.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.components.FunctionsMenu
import com.halead.catalog.components.ImageSelector
import com.halead.catalog.components.MaterialsMenu
import com.halead.catalog.components.TopBarFunctionType
import com.halead.catalog.data.enums.FunctionsEnum
import com.halead.catalog.screens.work.WorkHistoryPanel
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor

@Composable
fun MainScreen(
    selectedTopBarFunction: TopBarFunctionType? = null,
    mainViewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val mainUiState by mainViewModel.mainUiState.collectAsState()
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
    var applyMaterialTrigger by remember { mutableStateOf(false) } // bool value is just for triggering

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxHeight()
                    .weight(10f)
            ) {
                FunctionsMenu(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    canUndo = mainUiState.canUndo,
                    canRedo = mainUiState.canRedo,
                    baseImage = mainUiState.imageBmp,
                    isOverlaysEmpty = mainUiState.overlays.isEmpty(),
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
                    modifier = Modifier.weight(1f),
                    materials = mainUiState.materials,
                    imageBmp = mainUiState.imageBmp,
                    showImagePicker = showImagePickerDialog,
                    selectedMaterial = mainUiState.selectedMaterial,
                    overlays = mainUiState.overlays,
                    currentCursor = mainUiState.currentCursor,
                    changeImagePickerVisibility = { showImagePickerDialog = it },
                    applyMaterialTrigger = applyMaterialTrigger,
                ) {
                    mainViewModel.selectImage(it)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(50.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = mainUiState.selectedMaterial != null,
                    onClick = { applyMaterialTrigger = !applyMaterialTrigger },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mainUiState.isMaterialApplied) SelectedItemColor else ButtonColor,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (mainUiState.selectedMaterial == null) {
                        Text("Select Material", color = Color.White.copy(alpha = 0.4f))
                    } else if (mainUiState.isMaterialApplied){
                        Text("Material Applied", color = Color.White)
                    } else {
                        Text("Apply Material", color = Color.White)
                    }
                }
                MaterialsMenu(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color.Red),
                    materials = mainUiState.materials,
                    selectedMaterial = mainUiState.selectedMaterial,
                    onMaterialSelected = { mainViewModel.selectMaterial(it) }
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            when (selectedTopBarFunction) {
                TopBarFunctionType.MENU -> {}
                TopBarFunctionType.HISTORY -> {
                    WorkHistoryPanel(
                        onWorkClick = {
                            mainViewModel.bringHistoryWork(it)
                        }
                    )
                }

                TopBarFunctionType.SHARE -> {}
                TopBarFunctionType.SETTINGS -> {}
                null -> {}
            }
        }
    }
}
