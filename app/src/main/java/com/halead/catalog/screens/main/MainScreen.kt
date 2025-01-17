package com.halead.catalog.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.R
import com.halead.catalog.components.AutoSizeText
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
    viewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val mainUiState by viewModel.mainUiState.collectAsState()
    val loadingApplyMaterial by viewModel.loadingApplyMaterial.collectAsState()
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }
    val isPrimaryButtonEnabled by remember(mainUiState.selectedMaterial, mainUiState.imageBmp) {
        derivedStateOf {
            mainUiState.selectedMaterial != null && mainUiState.imageBmp != null
        }
    }
    val primaryButtonText by remember(mainUiState) {
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
                            viewModel.selectFunction(function)
                        }
                    },
                    onCursorClicked = { viewModel.selectCursor(it) }
                )
                ImageSelector(
                    modifier = Modifier.weight(1f),
                    imageBmp = mainUiState.imageBmp,
                    showImagePicker = showImagePickerDialog,
                    polygonPoints = mainUiState.polygonPoints,
                    overlays = mainUiState.overlays,
                    currentCursor = mainUiState.currentCursor,
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
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .heightIn(min = 50.dp)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = isPrimaryButtonEnabled,
                    onClick = { viewModel.applyMaterial() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mainUiState.isMaterialApplied &&  mainUiState.polygonPoints.size >= 3) SelectedItemColor else ButtonColor,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    val textColor = if (isPrimaryButtonEnabled) Color.White else Color.White.copy(alpha = 0.4f)

                    Row(
                        modifier = Modifier.wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AutoSizeText(
                            modifier = Modifier.weight(1f),
                            suggestedFontSizes = listOf(14.sp),
                            text = primaryButtonText,
                            color = textColor,
                            alignment = Alignment.Center
                        )
                        if (mainUiState.materials.isEmpty()) {
                            Spacer(Modifier.width(4.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        } else if (mainUiState.selectedMaterial == null) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                modifier = Modifier.size(16.dp),
                                tint = Color.White.copy(alpha = 0.4f),
                                painter = painterResource(R.drawable.ic_arrow_downward),
                                contentDescription = null
                            )
                        }
                    }
                }
                MaterialsMenu(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color.Red),
                    materials = mainUiState.materials,
                    selectedMaterial = mainUiState.selectedMaterial,
                    loadingApplyMaterial = loadingApplyMaterial,
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
