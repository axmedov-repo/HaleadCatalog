package com.halead.catalog.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.screens.main.MainViewModel
import com.halead.catalog.screens.main.MainViewModelImpl
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.findMinOffset
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.getClippedMaterial
import com.halead.catalog.utils.resizeBitmap
import com.halead.catalog.utils.timber
import kotlin.math.abs

@Composable
fun ImageEditor(
    materials: Map<String, Int>,
    imageBitmap: ImageBitmap,
    selectedMaterial: Int?,
    overlays: List<OverlayMaterialModel>,
    applyMaterialTrigger: Boolean,
    currentCursor: CursorData,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val context = LocalContext.current
    val mainUiState by viewModel.mainUiState.collectAsState()
    val selectedMaterialBmp by remember(selectedMaterial) {
        derivedStateOf {
            if (selectedMaterial != null && materials.containsValue(selectedMaterial)) {
                getBitmapFromResource(context, selectedMaterial)?.asImageBitmap()
            } else {
                null
            }
        }
    }

    var isDrawing by rememberSaveable { mutableStateOf(false) }
    val aspectRatio by remember(imageBitmap) {
        derivedStateOf { imageBitmap.width.toFloat() / imageBitmap.height.toFloat() }
    }

    val canIApplyMaterial by remember(mainUiState.polygonPoints, selectedMaterialBmp) {
        derivedStateOf {
            mainUiState.polygonPoints.isNotEmpty() && selectedMaterialBmp != null
        }
    }

    LaunchedEffect(applyMaterialTrigger) {
        if (canIApplyMaterial) {
            isDrawing = false

            val offsetOfOverlay = findMinOffset(mainUiState.polygonPoints)

            // Generate and add the overlay
            val appliedMaterialBitmap = getClippedMaterial(
                materialBitmap = resizeBitmap(selectedMaterialBmp!!.asAndroidBitmap(), 1024, 1024),
                regionPoints = mainUiState.polygonPoints
            )

            viewModel.addOverlay(
                OverlayMaterialModel(
                    materialBitmap = appliedMaterialBitmap,
                    regionPoints = mainUiState.polygonPoints,
                    position = offsetOfOverlay
                )
            )
        }
    }

    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio) // Maintain aspect ratio
            .fillMaxSize()
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {

        // Base image with aspect ratio scaling
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        // Draw overlays
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            overlays.forEach { overlay ->
                drawImage(overlay.materialBitmap.asImageBitmap(), topLeft = overlay.position)
            }
        }

        // Draw Region (polygon points)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(currentCursor) {
                    when (currentCursor.type) {
                        CursorTypes.DRAW -> {
                            detectTapGestures(onPress = { offset ->
                                viewModel.addPolygonPoint(offset)
                                isDrawing = true
                            })
                        }

                        CursorTypes.HAND -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    timber(
                                        "DRAG_GESTURE",
                                        "Drag started ${abs((offset - mainUiState.polygonPoints[1]).getDistance())}"
                                    )
                                    // Check if the touch is near any of the circles
                                    selectedPointIndex = mainUiState.polygonPoints.indexOfFirst { point ->
                                        (offset - point).getDistance() <= 30f // 8f is the circle radius
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    // Update the position of the selected circle
                                    val index = selectedPointIndex
                                    timber("DRAG_GESTURE", "Index=$index")
                                    if (index != -1) {
                                        viewModel.updatePolygonPoint(
                                            index,
                                            mainUiState.polygonPoints[index] + Offset(dragAmount.x, dragAmount.y)
                                        )
                                    }
                                    change.consume() // Consume the drag event
                                },
                                onDragEnd = {
                                    selectedPointIndex = -1 // Reset selection
                                    viewModel.memorizeUpdatedPolygonPoints()
                                }
                            )
                        }
                    }
                }
        ) {
            if (mainUiState.polygonPoints.isNotEmpty()) {
                for (i in 1 until mainUiState.polygonPoints.size) {
                    drawLine(
                        color = Color.Green,
                        start = mainUiState.polygonPoints[i - 1],
                        end = mainUiState.polygonPoints[i],
                        strokeWidth = 3f
                    )
                }

                if (mainUiState.polygonPoints.size > 1) {
                    drawLine(
                        color = Color.Green,
                        start = mainUiState.polygonPoints.last(),
                        end = mainUiState.polygonPoints.first(),
                        strokeWidth = 3f
                    )
                }

                mainUiState.polygonPoints.forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point, radius = 8f)
                }
            }
        }
    }
}
