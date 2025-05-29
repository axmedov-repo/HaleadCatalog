package com.halead.catalog.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.halead.catalog.R
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes
import com.halead.catalog.data.models.OverlayMaterialModel
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.isPanoramic
import com.halead.catalog.utils.isPointInPolygon
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.toAbsolute
import com.halead.catalog.utils.toRelative

@Composable
fun ImageEditor(
    imageBitmap: ImageBitmap,
    overlays: List<OverlayMaterialModel>,
    currentCursor: CursorData,
    polygonPoints: List<Offset>,
    editorSize: IntSize,
    modifier: Modifier = Modifier,
    onMainUiEvent: (MainUiEvent) -> Unit
) {
    val aspectRatio by remember(imageBitmap) {
        derivedStateOf { imageBitmap.width.toFloat() / imageBitmap.height.toFloat() }
    }

    val editorSizeOverlays by remember(overlays) {
        derivedStateOf {
            overlays.map {
                it.copy(
                    polygonPoints = it.polygonPoints.map { it.toAbsolute(editorSize) },
                    polygonCenter = it.polygonCenter.toAbsolute(editorSize),
                    holePoints = it.holePoints.map { it.map { it.toAbsolute(editorSize) } },
                    offset = it.offset.toAbsolute(editorSize),
                    overlay = it.overlay.toAbsolute(editorSize)
                )
            }
        }
    }

    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    var arrowPos by remember { mutableStateOf<Offset?>(null) }
    val arrowOffset = Offset(-180f, -80f)

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .fillMaxSize()
            .padding(8.dp)
            .onSizeChanged {
                onMainUiEvent(MainUiEvent.EditorSize(it))
            },
        contentAlignment = Alignment.Center
    ) {

        // Base Image
        Image(
            bitmap = imageBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .matchParentSize()
                .clip(RoundedCornerShape(4.dp))
        )

        // Overlays
        Box(
            modifier = Modifier.matchParentSize(), contentAlignment = Alignment.TopStart
        ) {
            if (editorSizeOverlays.isNotEmpty()) {
                editorSizeOverlays.forEach { overlayData ->
                    val materialBmp =
                        getBitmapFromResource(LocalContext.current, overlayData.material)
                    /*val state =
                        rememberTransformableState { zoomChange, offsetChange, rotationChange ->
                            timber("CustomTransformable", "changing")
                            onMainUiEvent(
                                MainUiEvent.UpdateCurrentOverlayTransform(
                                    zoomChange,
                                    offsetChange.toRelative(editorSize),
                                    rotationChange
                                )
                            )
                        }*/
                    timber("OVERLAY_DATA", "offset=${overlayData.offset}")
                    if (materialBmp?.isPanoramic() == true) { // TODO: Handle panoramic materials
                        PerspectiveScrollableImage(
                            bitmap = materialBmp,
                            outerPolygon = overlayData.polygonPoints,
                            holes = overlayData.holePoints
                        )
                    } else {
                        Image(
                            bitmap = overlayData.overlay.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .graphicsLayer {
                                    // Apply transformations relative to the center of the overlay
                                    val pivotX = overlayData.polygonCenter.x - overlayData.offset.x
                                    val pivotY = overlayData.polygonCenter.y - overlayData.offset.y

                                    translationX = overlayData.offset.x
                                    translationY = overlayData.offset.y

                                    rotationZ = overlayData.rotation
                                    scaleX = overlayData.scale
                                    scaleY = overlayData.scale

                                    transformOrigin = TransformOrigin(
                                        pivotX / overlayData.overlay.width,
                                        pivotY / overlayData.overlay.height
                                    )
                                }
//                                .clickable { onMainUiEvent(MainUiEvent.SelectOverlay(overlayData)) }
                            /* .pointerInput(currentCursor) {
                                 if (currentCursor.type == CursorTypes.DRAG_PAN) {
                                     detectDragGestures(
                                         onDragStart = {
                                             onMainUiEvent(MainUiEvent.SelectOverlay(overlayData))
                                         },
                                         onDrag = { change, dragAmount ->
                                             CoroutineScope(Dispatchers.Main).launch {
                                                 onMainUiEvent(
                                                     MainUiEvent.UpdateCurrentOverlayPosition(
                                                         overlayIndex = null,
                                                         dragAmount = Offset(dragAmount.x, dragAmount.y)
                                                     )
                                                 )
                                                 change.consume()
                                             }
                                         },
                                         onDragEnd = {
                                             onMainUiEvent(MainUiEvent.MemorizeUpdatedPolygonPoints)
                                         }
                                     )
                                 }
                             }*/
//                                .transformable(state = state)
                            ,
                            contentScale = ContentScale.None
                        )
                    }
                }
                LaunchedEffect(overlays) {
                    onMainUiEvent(MainUiEvent.AllOverlaysDrawn)
                }
            } else {
                LaunchedEffect(overlays) {
                    onMainUiEvent(MainUiEvent.AllOverlaysDrawn)
                }
            }
        }

        // Draw Region (polygon points)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(currentCursor) {
                    when (currentCursor.type) {
                        CursorTypes.DRAW_INSERT -> {
////                            detectTapGestures(onPress = { offset ->
////                                onMainUiEvent(MainUiEvent.InsertPolygonPoint(offset.toRelative(editorSize)))
////                            })
//
//                            awaitPointerEventScope {
//                                while (true) {
//                                    val down = awaitFirstDown()
//                                    arrowPos = down.position + arrowOffset
//
//                                    var released = false
//                                    while (!released) {
//                                        val event = awaitPointerEvent()
//                                        val change = event.changes.first()
//
//                                        if (change.pressed) {
//                                            arrowPos = change.position + arrowOffset
//                                        } else if (change.changedToUp()) {
//                                            released = true
//                                            arrowPos?.let {
//                                                onMainUiEvent(MainUiEvent.InsertPolygonPoint(it.toRelative(editorSize)))
//                                            }
//                                            arrowPos = null
//                                        }
//                                }
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown()
                                    val inputType = down.type  // This detects stylus vs finger
                                    val position = down.position

                                    if (inputType == PointerType.Stylus) {
                                        // Stylus: use instant tap behavior (more precise)
                                        onMainUiEvent(
                                            MainUiEvent.InsertPolygonPoint(position.toRelative(editorSize))
                                        )
                                    } else {
                                        // Finger: use visual arrow + release-to-confirm logic
                                        arrowPos = position + arrowOffset
                                        var released = false

                                        while (!released) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()

                                            if (change.pressed) {
                                                arrowPos = change.position + arrowOffset
                                            } else if (change.changedToUp()) {
                                                released = true
                                                arrowPos?.let {
                                                    onMainUiEvent(
                                                        MainUiEvent.InsertPolygonPoint(it.toRelative(editorSize))
                                                    )
                                                }
                                                arrowPos = null
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        CursorTypes.DRAW_EXTEND -> {
                            detectTapGestures(onPress = { offset ->
                                onMainUiEvent(MainUiEvent.ExtendPolygonPoints(offset.toRelative(editorSize)))
                            })
                        }

                        CursorTypes.DRAG_PAN -> {
                            detectDragGestures(
                                onDragStart = { offset ->
//                                    CoroutineScope(Dispatchers.Default).launch {
                                    selectedPointIndex = polygonPoints.indexOfFirst { point ->
                                        (offset - point.toAbsolute(editorSize)).getDistance() <= 32f
                                    }
                                    timber(
                                        "CustomTransformable",
                                        " CursorTypes.DRAG_PAN listening $selectedPointIndex"
                                    )
                                    if (selectedPointIndex == -1) {
                                        // If not near a point, return false to allow underlying gestures
                                        return@detectDragGestures
                                    }
//                                    }
                                },
                                onDrag = { change, dragAmount ->
//                                    CoroutineScope(Dispatchers.Main).launch {
                                    if (selectedPointIndex != -1) {
                                        onMainUiEvent(
                                            MainUiEvent.UpdatePolygonPoint(
                                                selectedPointIndex,
                                                (polygonPoints[selectedPointIndex].toAbsolute(editorSize) + Offset(
                                                    dragAmount.x,
                                                    dragAmount.y
                                                )).toRelative(editorSize)
                                            )
                                        )
                                        change.consume() // Consume the drag event
                                    }
//                                    }
                                },
                                onDragEnd = {
                                    if (selectedPointIndex != -1) {
                                        selectedPointIndex = -1 // Reset selection
                                        onMainUiEvent(MainUiEvent.MemorizeUpdatedPolygonPoints)
                                    }
                                }
                            )
                        }
                    }
                }
                .then(
                    if (currentCursor.type == CursorTypes.DRAG_PAN) {
                        Modifier.pointerInput(currentCursor, editorSizeOverlays) {
                            if (currentCursor.type == CursorTypes.DRAG_PAN) {
                                detectTapGestures(onTap = { point ->
                                    for (index in editorSizeOverlays.size - 1 downTo 0) {
                                        val overlay = editorSizeOverlays[index]
                                        if (isPointInPolygon(
                                                point,
                                                overlay.polygonPoints,
                                                overlay.holePoints
                                            )
                                        ) {
                                            onMainUiEvent(MainUiEvent.SelectOverlay(overlay))
                                            return@detectTapGestures
                                        }
                                    }
                                    if (polygonPoints.isNotEmpty()) {
                                        onMainUiEvent(MainUiEvent.UnselectCurrentOverlay)
                                    }
                                })
                            }
                        }
                    } else Modifier
                )
        ) {
            if (polygonPoints.size > 1) {
                drawLine(
                    color = Color.Green,
                    start = polygonPoints.last().toAbsolute(editorSize),
                    end = polygonPoints.first().toAbsolute(editorSize),
                    strokeWidth = 3f
                )
            }

            if (polygonPoints.size > 2) {
                polygonPoints.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = Color.Green,
                        start = start.toAbsolute(editorSize),
                        end = end.toAbsolute(editorSize),
                        strokeWidth = 3f
                    )
                }
            }

            if (polygonPoints.isNotEmpty()) {
                polygonPoints.forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point.toAbsolute(editorSize), radius = 8f)
                }
            }

//            arrowPos?.let {
//               drawArrowAt(it)
//            }
        }

        arrowPos?.let { position ->
            Box(Modifier.matchParentSize()) {
                Icon(
                    painter = painterResource(R.drawable.ic_stylus),
                    contentDescription = "Pointer",
                    tint = SelectedItemColor,
                    modifier = Modifier
                        .offset {
                            IntOffset(position.x.toInt(), position.y.toInt() - 64)
                        }
                        .size(32.dp)
                        .rotate(45f)
                )
            }
        }
    }
}

private fun DrawScope.drawArrowAt(center: Offset) {
    val size = 40f
    val path = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - size / 2, center.y + size)
        lineTo(center.x + size / 2, center.y + size)
        close()
    }
    drawPath(path, color = Color.Red)
}
