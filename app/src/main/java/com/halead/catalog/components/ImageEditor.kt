package com.halead.catalog.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.halead.catalog.R
import com.halead.catalog.data.enums.CursorData
import com.halead.catalog.data.enums.CursorTypes
import com.halead.catalog.data.models.OverlayData
import com.halead.catalog.ui.events.MainUiEvent
import com.halead.catalog.ui.theme.SelectedItemColor
import com.halead.catalog.utils.dpToIntPx
import com.halead.catalog.utils.isPanoramic
import com.halead.catalog.utils.isPointInPolygon
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.toAbsolute
import com.halead.catalog.utils.toDp
import com.halead.catalog.utils.toIntPx
import com.halead.catalog.utils.toRelative
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ImageEditor(
    imageBitmap: () -> ImageBitmap,
    overlays: () -> ImmutableList<OverlayData>,
    currentCursor: () -> CursorData,
    polygonPoints: () -> ImmutableList<Offset>,
    editorSize: () -> IntSize,
    modifier: Modifier = Modifier,
    onMainUiEvent: (MainUiEvent) -> Unit
) {
    val currentEditorSize = editorSize()
    val currentOverlays = overlays()

    val editorSizeOverlays = remember(currentOverlays, currentEditorSize) {
        currentOverlays.map {
            it.copy(
                polygonPoints = it.polygonPoints.map { it.toAbsolute(currentEditorSize) }.toImmutableList(),
                polygonCenter = it.polygonCenter.toAbsolute(currentEditorSize),
                holePoints = it.holePoints.map { it.map { it.toAbsolute(currentEditorSize) } }.toImmutableList(),
                offset = it.offset.toAbsolute(currentEditorSize),
                overlay = it.overlay.asAndroidBitmap().toAbsolute(currentEditorSize).asImageBitmap()
            )
        }
    }

    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    var pointerOffset by remember { mutableStateOf<Offset?>(null) }
    val arrowOffset = Offset(-180f, -80f)

    Box(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .width(IntrinsicSize.Max)
            .padding(8.dp)
            .onSizeChanged {
                onMainUiEvent(MainUiEvent.SaveEditorSize(it))
                timber(
                    "FullScreenIcon",
                    "ImageEditorSize=${it.width}, ${it.height}"
                )
            },
//            .layout { measurable, constraints ->
//                val placeable = measurable.measure(constraints)
//                val sizeAfterPadding = IntSize(placeable.width - dpToIntPx(16.dp), placeable.height - dpToIntPx(16.dp))
//                onMainUiEvent(MainUiEvent.SaveEditorSize(sizeAfterPadding))
//                timber(
//                    "FullScreenIcon",
//                    "currentEditorSize($currentEditorSize)==actualSize(${sizeAfterPadding.width}, ${sizeAfterPadding.height})"
//                )
//
//                layout(placeable.width, placeable.height) {
//                    placeable.place(0, 0)
//                }
//            },
        contentAlignment = Alignment.Center
    ) {

        // Base Image
        Image(
            bitmap = imageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .onSizeChanged {
                    timber(
                        "FullScreenIcon",
                        "ImageSize=${it.width}, ${it.height}"
                    )
                }
//                .layout { measurable, constraints ->
//                    val placeable = measurable.measure(constraints)
//
//                    timber(
//                        "FullScreenIcon",
//                        "ImageSize=${placeable.width}, ${placeable.height}"
//                    )
//
//                    layout(placeable.width, placeable.height) {
//                        placeable.place(0, 0)
//                    }
//                }
        )

        // Overlays
        Overlays(
            modifier = Modifier.matchParentSize(),
            overlays = editorSizeOverlays,
            onMainUiEvent = onMainUiEvent
        )

        val updatedCursor = rememberUpdatedState(currentCursor())
        val updatedPolygonPoints = rememberUpdatedState(polygonPoints())

        // Draw Region (polygon points)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged {
                    timber(
                        "FullScreenIcon",
                        "CanvasSize=${it.width}, ${it.height}"
                    )
                }
                .pointerInput(updatedCursor.value, currentEditorSize) {
                    if (updatedCursor.value.type == CursorTypes.DRAG_PAN) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val relativeOffset = offset.toRelative(currentEditorSize)
//                                    CoroutineScope(Dispatchers.Default).launch {
                                selectedPointIndex = updatedPolygonPoints.value.indexOfFirst { point ->
                                    (relativeOffset - point).getDistance() <= 32f
                                }
                                timber(
                                    "CustomTransformable",
                                    " CursorTypes.DRAG_PAN listening $selectedPointIndex"
                                )
                                if (selectedPointIndex == -1) {
                                    return@detectDragGestures
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (selectedPointIndex != -1) {
                                    onMainUiEvent(
                                        MainUiEvent.UpdatePolygonPoint(
                                            selectedPointIndex,
                                            (updatedPolygonPoints.value[selectedPointIndex].toAbsolute(
                                                currentEditorSize
                                            ) + Offset(
                                                dragAmount.x,
                                                dragAmount.y
                                            )).toRelative(currentEditorSize)
                                        )
                                    )
                                    change.consume() // Consume the drag event
                                }
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
                .pointerInput(updatedCursor.value, currentEditorSize) {
                    awaitPointerEventScope {
                        while (true) {
                            val cursor = updatedCursor.value
                            val down = awaitFirstDown()
                            val inputType = down.type
                            val position = down.position

                            when (cursor.type) {
                                CursorTypes.DRAW_INSERT -> {
                                    if (inputType == PointerType.Stylus) {
                                        onMainUiEvent(
                                            MainUiEvent.InsertPolygonPoint(
                                                position.toRelative(
                                                    currentEditorSize
                                                )
                                            )
                                        )
                                    } else {
                                        pointerOffset = position + arrowOffset
                                        var released = false

                                        while (!released) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()

                                            if (change.pressed) {
                                                pointerOffset = change.position + arrowOffset
                                            } else if (change.changedToUp()) {
                                                released = true
                                                pointerOffset?.let {
                                                    onMainUiEvent(
                                                        MainUiEvent.InsertPolygonPoint(
                                                            it.toRelative(
                                                                currentEditorSize
                                                            )
                                                        )
                                                    )
                                                }
                                                pointerOffset = null
                                            }
                                        }
                                    }
                                }

                                CursorTypes.DRAW_EXTEND -> {
                                    if (inputType == PointerType.Stylus) {
                                        onMainUiEvent(
                                            MainUiEvent.ExtendPolygonPoints(
                                                position.toRelative(
                                                    currentEditorSize
                                                )
                                            )
                                        )
                                    } else {
                                        pointerOffset = position + arrowOffset
                                        var released = false

                                        while (!released) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.first()

                                            if (change.pressed) {
                                                pointerOffset = change.position + arrowOffset
                                            } else if (change.changedToUp()) {
                                                released = true
                                                pointerOffset?.let {
                                                    onMainUiEvent(
                                                        MainUiEvent.ExtendPolygonPoints(
                                                            it.toRelative(
                                                                currentEditorSize
                                                            )
                                                        )
                                                    )
                                                }
                                                pointerOffset = null
                                            }
                                        }
                                    }
                                }

                                else -> {}
                            }
                        }
                    }
                }
                .then(
                    if (updatedCursor.value.type == CursorTypes.DRAG_PAN) {
                        Modifier.pointerInput(updatedCursor.value, editorSizeOverlays) {
                            if (updatedCursor.value.type == CursorTypes.DRAG_PAN) {
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
                                    if (updatedPolygonPoints.value.isNotEmpty()) {
                                        onMainUiEvent(MainUiEvent.UnselectCurrentOverlay)
                                    }
                                })
                            }
                        }
                    } else Modifier
                )
        ) {
            if (updatedPolygonPoints.value.size > 1) {
                drawLine(
                    color = Color.Green,
                    start = updatedPolygonPoints.value.last().toAbsolute(currentEditorSize),
                    end = updatedPolygonPoints.value.first().toAbsolute(currentEditorSize),
                    strokeWidth = 3f
                )
            }

            if (updatedPolygonPoints.value.size > 2) {
                updatedPolygonPoints.value.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = Color.Green,
                        start = start.toAbsolute(currentEditorSize),
                        end = end.toAbsolute(currentEditorSize),
                        strokeWidth = 3f
                    )
                }
            }

            if (updatedPolygonPoints.value.isNotEmpty()) {
                updatedPolygonPoints.value.forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point.toAbsolute(currentEditorSize), radius = 8f)
                }
            }
        }

        Pointer(pointerOffset, modifier = Modifier.matchParentSize())
    }
}

@Composable
private fun Pointer(offset: Offset?, modifier: Modifier = Modifier) {
    offset?.let { position ->
        val offsetAfterRotation = 16.dp.toIntPx()   // icon size = 32.dp, rotation = 45, 32 / 2 = 16
        Box(modifier) {
            Icon(
                painter = painterResource(R.drawable.ic_stylus),
                contentDescription = "Pointer",
                tint = SelectedItemColor,
                modifier = Modifier
                    .offset { IntOffset(position.x.toInt(), position.y.toInt() - offsetAfterRotation) }
                    .size(32.dp)
                    .rotate(45f)
            )
        }
    }
}

@Composable
private fun Overlays(
    overlays: List<OverlayData>,
    onMainUiEvent: (MainUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.TopStart
    ) {
        if (overlays.isNotEmpty()) {
            overlays.forEach { overlayData ->
                Overlay(overlayData)
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
}

@Composable
private fun Overlay(
    overlayData: OverlayData,
    modifier: Modifier = Modifier
) {
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
    val overlayMaterialBitmap = overlayData.material?.bitmap

    if (overlayMaterialBitmap?.isPanoramic() == true) { // TODO: Handle panoramic materials
        PerspectiveScrollableImage(
            modifier = modifier,
            bitmap = overlayMaterialBitmap.asAndroidBitmap(),
            outerPolygon = overlayData.polygonPoints,
            holes = overlayData.holePoints
        )
    } else {
        Image(
            bitmap = overlayData.overlay,
            contentDescription = null,
            modifier = modifier
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
