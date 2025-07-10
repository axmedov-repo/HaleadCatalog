package com.halead.catalog.screens.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntSize
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
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.ButtonColor
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.canMakeClosedShape
import com.halead.catalog.utils.dpToPx
import com.halead.catalog.utils.timber

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val uiState by viewModel.mainUiState.collectAsState()
    val loadingApplyMaterialState by viewModel.loadingApplyMaterialState.collectAsState()
    val currentCursorState by viewModel.currentCursorState.collectAsState()
    val perspectiveSwitchValueState by viewModel.isPerspectiveEnabled.collectAsState()
    val editorScreenState by viewModel.editorScreenState.collectAsState()

    LaunchedEffect(uiState.overlays) {
        timber("OverlaysLog", "${uiState.overlays}")
    }

    LaunchedEffect(editorScreenState.isFullScreen) {
        timber("FullScreenIcon", "editorScreenState=$editorScreenState")
    }

    val context = LocalContext.current
    var imageSelectingPurpose by remember { mutableStateOf(ImageSelectingPurpose.EDITING_IMAGE) }
    var showImagePickerDialog by rememberSaveable { mutableStateOf(false) }

    val rememberOnFunctionClicked = remember {
        { function: FunctionData ->
            if (function.type == FunctionsEnum.RESET_IMAGE) {
                imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                showImagePickerDialog = true
            } else {
                viewModel.onUiEvent(MainUiEvent.SelectFunction(function))
            }
        }
    }

    val rememberOnCursorClicked = remember {
        { cursorData: CursorData ->
            viewModel.onUiEvent(MainUiEvent.SelectCursor(cursorData))
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
            canUndo = uiState.canUndo,
            canRedo = uiState.canRedo,
            baseImage = uiState.imageBmp,
            overlays = uiState.overlays,
            polygonPointsSize = { uiState.polygonPoints.size },
            selectedOverlay = uiState.currentOverlay,
            selectedCursor = currentCursorState,
            onFunctionClicked = rememberOnFunctionClicked,
            onCursorClicked = rememberOnCursorClicked
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .onSizeChanged {
                    viewModel.onUiEvent(MainUiEvent.SaveEditorFullSize(it))
                    timber("FullScreenIcon", "FullScreen size = ${it.width}, ${it.height}")
                }
//                .layout { measurable, constraints ->
//                    val placeable = measurable.measure(constraints)
//
//                    viewModel.onUiEvent(MainUiEvent.SaveEditorFullSize(IntSize(placeable.width, placeable.height)))
//                    timber("FullScreenIcon", "FullScreen size = ${placeable.width}, ${placeable.height}")
//                    timber("FullScreenIcon", "FullScreen size - 32.dp, 16.dp= ${placeable.width - dpToPx(context, 32f)}, ${placeable.height - dpToPx(context, 16f)}")
//
//                    layout(placeable.width, placeable.height) {
//                        placeable.place(0, 0)
//                    }
//                }
        ) {
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
                            .wrapContentHeight()
                            .onSizeChanged {
                                viewModel.onUiEvent(MainUiEvent.SaveHeightDiffOfFullScreen(it.height))
                            },
//                            .layout { measurable, constraints ->
//                                val placeable = measurable.measure(constraints)
//
//                                viewModel.onUiEvent(MainUiEvent.SaveHeightDiffOfFullScreen(placeable.height))
//
//                                layout(placeable.width, placeable.height) {
//                                    placeable.place(0, 0)
//                                }
//                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FunctionsMenu(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight(),
                            canUndo = uiState.canUndo,
                            canRedo = uiState.canRedo,
                            baseImage = uiState.imageBmp,
                            overlays = uiState.overlays,
                            polygonPointsSize = { uiState.polygonPoints.size },
                            selectedOverlay = uiState.currentOverlay,
                            onFunctionClicked = rememberOnFunctionClicked,
                        )
                        val rememberOnCheckedChange = remember {
                            { value: Boolean ->
                                viewModel.onUiEvent(MainUiEvent.ChangePerspective(value))
                            }
                        }
                        PerspectiveSwitch(
                            modifier = Modifier.wrapContentWidth(),
                            isChecked = { perspectiveSwitchValueState },
                            onCheckedChange = rememberOnCheckedChange
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = if (editorScreenState.isFullScreen) 8.dp else 0.dp)
                ) {
                    val onChangeImagePickerValue = remember {
                        { value: Boolean ->
                            imageSelectingPurpose = ImageSelectingPurpose.EDITING_IMAGE
                            showImagePickerDialog = value
                        }
                    }

                    ImageSelector(
                        imageBmp = uiState.imageBmp,
                        purpose = imageSelectingPurpose,
                        showImagePicker = showImagePickerDialog,
                        polygonPoints = { uiState.polygonPoints },
                        overlays = uiState.overlays,
                        currentCursor = currentCursorState,
                        editorSize = { editorScreenState.size },
                        changeImagePickerVisibility = onChangeImagePickerValue,
                        onMainUiEvent = viewModel::onUiEvent
                    )

                    if (uiState.editorHasImage) {
                        val onClick = remember {
                            {
                                viewModel.onUiEvent(MainUiEvent.OnFullScreenChanged)
                            }
                        }
                        FullScreenIcon(
                            editorScreenState.isFullScreen,
                            modifier = Modifier.align(Alignment.TopEnd),
                            onClick = onClick
                        )
                    }
                }
            }

            if (!editorScreenState.isFullScreen) {
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .onSizeChanged {
                            viewModel.onUiEvent(MainUiEvent.SaveWidthDiffOfFullScreen(it.width))
                        },
//                        .layout { measurable, constraints ->
//                            val placeable = measurable.measure(constraints)
//
//                            viewModel.onUiEvent(MainUiEvent.SaveWidthDiffOfFullScreen(placeable.width))
//
//                            layout(placeable.width, placeable.height) {
//                                placeable.place(0, 0)
//                            }
//                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isPrimaryButtonEnabled by remember {
                        derivedStateOf {
                            uiState.selectedMaterial != null && uiState.imageBmp != null && uiState.polygonPoints.canMakeClosedShape()
                        }
                    }

                    val primaryButtonTextResId by remember {
                        derivedStateOf {
                            when {
                                uiState.materials.isEmpty() -> R.string.loading_materials
                                uiState.selectedMaterial == null -> R.string.select_material
                                !uiState.polygonPoints.canMakeClosedShape() -> R.string.select_region
                                uiState.isMaterialApplied -> R.string.material_applied
                                else -> R.string.apply_material
                            }
                        }
                    }

                    val primaryButtonContainerColor by remember {
                        derivedStateOf {
                            if (uiState.isMaterialApplied && uiState.polygonPoints.canMakeClosedShape()) SelectedItemColor else ButtonColor
                        }
                    }

                    val rememberOnClick = remember {
                        { viewModel.onUiEvent(MainUiEvent.ApplyMaterial) }
                    }

                    PrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        primaryButtonText = { context.getString(primaryButtonTextResId) },
                        isPrimaryButtonEnabled = { isPrimaryButtonEnabled },
                        isMaterialsEmpty = { uiState.materials.isEmpty() },
                        containerColor = { primaryButtonContainerColor },
                        onClick = rememberOnClick
                    )

                    val rememberOnAddMaterial = remember {
                        {
                            imageSelectingPurpose = ImageSelectingPurpose.ADD_MATERIAL
                            showImagePickerDialog = true
                        }
                    }

                    val rememberOnMaterialSelected = remember {
                        { selectedMaterial: Int ->
                            viewModel.onUiEvent(MainUiEvent.SelectMaterial(selectedMaterial))
                        }
                    }

                    MaterialsMenu(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxHeight(),
                        materials = uiState.materials,
                        selectedMaterial = uiState.selectedMaterial,
                        loadingApplyMaterial = { loadingApplyMaterialState },
                        onAddMaterial = rememberOnAddMaterial,
                        onMaterialSelected = rememberOnMaterialSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenIcon(
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
