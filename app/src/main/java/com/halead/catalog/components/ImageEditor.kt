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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.screens.main.MainViewModel
import com.halead.catalog.screens.main.MainViewModelImpl
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.getPolygonHull
import com.halead.catalog.utils.isPointInPolygon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ImageEditor(
    imageBitmap: ImageBitmap,
    overlays: List<OverlayMaterialModel>,
    currentCursor: CursorData,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel<MainViewModelImpl>()
) {
    val mainUiState by viewModel.mainUiState.collectAsState()
    var selectionJob: Job? = null
    val aspectRatio by remember(imageBitmap) {
        derivedStateOf { imageBitmap.width.toFloat() / imageBitmap.height.toFloat() }
    }

    var selectedPointIndex by remember { mutableIntStateOf(-1) }
    var currentOverlay = remember<Pair<Int, OverlayMaterialModel>?> { null }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {

        // Base Image
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        // Overlays
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
            if (overlays.isNotEmpty()) {
                overlays.forEach { overlay ->
                    Image(
                        bitmap = overlay.overlay.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer(
                                translationX = overlay.position.x,
                                translationY = overlay.position.y
                            ),
                        contentScale = ContentScale.None
                    )
                }
                LaunchedEffect(overlays) {
                    viewModel.allOverlaysDrawn()
                }
            } else {
                LaunchedEffect(overlays) {
                    viewModel.allOverlaysDrawn()
                }
            }
        }

        // Draw Region (polygon points)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(currentCursor) {
                    when (currentCursor.type) {
                        CursorTypes.DRAW_INSERT -> {
                            detectTapGestures(onPress = { offset ->
                                viewModel.insertPolygonPoint(offset)
                            })
                        }

                        CursorTypes.DRAW_EXTEND -> {
                            detectTapGestures(onPress = { offset ->
                                viewModel.extendPolygonPoints(offset)
                            })
                        }

                        CursorTypes.DRAG_PAN -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    selectionJob?.cancel()
                                    selectionJob = CoroutineScope(Dispatchers.Default).launch {
                                        val selectedOverlay = viewModel.mainUiState.value.overlays
                                            .asReversed()
                                            .firstOrNull { isPointInPolygon(offset, it.polygonPoints, it.holePoints) }

                                        withContext(Dispatchers.Main) {
                                            selectedPointIndex = mainUiState.polygonPoints.indexOfFirst { point ->
                                                (offset - point).getDistance() <= 32f
                                            }

                                            if (selectedPointIndex == -1) {
                                                selectedOverlay?.let {
                                                    viewModel.selectOverlay(it)
                                                    currentOverlay = Pair(viewModel.mainUiState.value.overlays.indexOf(it), it)
                                                }
                                            }
                                        }

                                        selectionJob = null
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        selectionJob?.join()
                                        // Update the position of the selected circle
                                        if (selectedPointIndex != -1) {
                                            viewModel.updatePolygonPoint(
                                                selectedPointIndex,
                                                mainUiState.polygonPoints[selectedPointIndex] + Offset(dragAmount.x, dragAmount.y)
                                            )
                                            change.consume() // Consume the drag event
                                        } else if (currentOverlay != null) {
                                            viewModel.updateCurrentOverlayPosition(
                                                overlayIndex = currentOverlay!!.first,
                                                dragAmount = Offset(dragAmount.x, dragAmount.y)
                                            )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (selectedPointIndex != -1 || currentOverlay != null) {
                                        selectedPointIndex = -1 // Reset selection
                                        currentOverlay = null
                                        viewModel.memorizeUpdatedPolygonPoints()
                                    }
                                    selectionJob = null
                                }
                            )
                        }
                    }
                }
                .pointerInput(currentCursor, overlays) {
                    if (currentCursor.type == CursorTypes.DRAG_PAN) {
                        detectTapGestures(onTap = { point ->
                            for (index in overlays.size - 1 downTo 0) {
                                val overlay = overlays[index]
                                if (isPointInPolygon(point, overlay.polygonPoints, overlay.holePoints)) {
                                    viewModel.selectOverlay(overlay)
                                    return@detectTapGestures
                                }
                            }
                            if (mainUiState.polygonPoints.isNotEmpty()) {
                                viewModel.unselectCurrentOverlay()
                            }
                        })
                    }
                }
        ) {
            if (mainUiState.polygonPoints.size > 1) {
                drawLine(
                    color = Color.Green,
                    start = mainUiState.polygonPoints.last(),
                    end = mainUiState.polygonPoints.first(),
                    strokeWidth = 3f
                )
            }

            if (mainUiState.polygonPoints.size > 2) {
                mainUiState.polygonPoints.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = Color.Green,
                        start = start,
                        end = end,
                        strokeWidth = 3f
                    )
                }
            }

            if (mainUiState.polygonPoints.isNotEmpty()) {
                mainUiState.polygonPoints.forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point, radius = 8f)
                }
            }

            val rectCorners = getPolygonHull(mainUiState.polygonPoints)
            if (rectCorners.size > 1) {
                drawLine(
                    color = Color.Red,
                    start = rectCorners.last(),
                    end = rectCorners.first(),
                    strokeWidth = 3f
                )
            }

            if (rectCorners.size > 2) {
                rectCorners.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = Color.Red,
                        start = start,
                        end = end,
                        strokeWidth = 3f
                    )
                }
            }

            if (rectCorners.isNotEmpty()) {
                rectCorners.forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point, radius = 8f)
                }
            }
        }
    }
}
