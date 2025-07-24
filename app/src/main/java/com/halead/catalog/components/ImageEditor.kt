package com.halead.catalog.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
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
import com.halead.catalog.utils.isPanoramic
import com.halead.catalog.utils.isPointInPolygon
import com.halead.catalog.utils.timber
import com.halead.catalog.utils.toAbsolute
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
            },
        contentAlignment = Alignment.Center
    ) {

        // Base Image
        Image(
            bitmap = imageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
        )

        // Overlays
        Overlays(
            modifier = Modifier.matchParentSize(),
            overlays = editorSizeOverlays,
            currentCursor = currentCursor,
            editorSize = editorSize,
            onMainUiEvent = onMainUiEvent
        )

        val updatedCursor = rememberUpdatedState(currentCursor())
        val updatedPolygonPoints = rememberUpdatedState(polygonPoints())

        // Draw Region (polygon points)
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (updatedCursor.value.type == CursorTypes.DRAG_PAN) {
                        Modifier
                            .pointerInput(updatedCursor.value, editorSizeOverlays) {
                                awaitPointerEventScope {
                                    UpperWhileBlock@ while (true) {
                                        val baseEvent = awaitPointerEvent(pass = PointerEventPass.Initial)
                                        val activeTouchCount = baseEvent.changes.count { it.pressed }
                                        timber("CustomTransformable", "Tap activeTouchCount=$activeTouchCount")
                                        if (activeTouchCount == 1) {
                                            // Use a different pass to avoid conflicts with drag handling
                                            val down = awaitFirstDown()

                                            // Wait for up or cancellation
                                            val pointerId = down.id
                                            var up: Boolean

                                            while (true) {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.firstOrNull { it.id == pointerId } ?: continue

                                                val touchCount = event.changes.count { it.pressed }
                                                timber("CustomTransformable", "Tap touchCount=$touchCount")
                                                if (touchCount in 0..1) {
                                                    if (change.changedToUp()) {
                                                        up = true
                                                        break
                                                    }

                                                    if (!change.pressed) {
                                                        up = false
                                                        break
                                                    }
                                                } else {
                                                    continue@UpperWhileBlock
                                                }
                                            }

                                            if (up) {
                                                val position = down.position

                                                // Check if tap is on a polygon point first
                                                val relativeOffset = position.toRelative(currentEditorSize)
                                                val index = updatedPolygonPoints.value.indexOfFirst {
                                                    (relativeOffset - it).getDistance() <= 32f
                                                }

                                                if (index == -1) {
                                                    // Only handle overlay selection if not on a polygon point
                                                    timber("CustomTransformable", "Tap at $position")
                                                    var hit = false
                                                    for (overlayIdx in editorSizeOverlays.size - 1 downTo 0) {
                                                        val overlay = editorSizeOverlays[overlayIdx]
                                                        if (isPointInPolygon(position, overlay.polygonPoints, overlay.holePoints)
                                                        ) {
                                                            val relativeOverlay = overlays()[overlayIdx]
                                                            onMainUiEvent(MainUiEvent.SelectOverlay(relativeOverlay))
                                                            hit = true
                                                            break
                                                        }
                                                    }

                                                    if (!hit && updatedPolygonPoints.value.isNotEmpty()) {
                                                        onMainUiEvent(MainUiEvent.UnselectCurrentOverlay)
                                                    }
                                                } else {
                                                    // Handle point drag
                                                    // Consume the initial event to prevent other handlers
                                                    down.consume()
                                                    selectedPointIndex = index
                                                    var currentPosition = position

                                                    while (true) {
                                                        val dragEvent = awaitPointerEvent()
                                                        val change = dragEvent.changes.first()

                                                        if (change.pressed) {
                                                            val delta = change.position - currentPosition
                                                            currentPosition = change.position

                                                            val newAbs =
                                                                updatedPolygonPoints.value[selectedPointIndex]
                                                                    .toAbsolute(currentEditorSize) + delta

                                                            onMainUiEvent(
                                                                MainUiEvent.UpdatePolygonPoint(
                                                                    selectedPointIndex,
                                                                    newAbs.toRelative(currentEditorSize)
                                                                )
                                                            )

                                                            change.consume()
                                                        } else if (change.changedToUp()) {
                                                            selectedPointIndex = -1
                                                            onMainUiEvent(MainUiEvent.SaveCurrentState)
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .pointerInteropFilter { false }
                    } else if (updatedCursor.value.type == CursorTypes.DRAW_INSERT || updatedCursor.value.type == CursorTypes.DRAW_EXTEND) {
                        Modifier.pointerInput(updatedCursor.value, currentEditorSize) {
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
                    } else Modifier
                )
        ) {
            if (polygonPoints().size > 1) {
                drawLine(
                    color = Color.Green,
                    start = polygonPoints().last().toAbsolute(currentEditorSize),
                    end = polygonPoints().first().toAbsolute(currentEditorSize),
                    strokeWidth = 3f
                )
            }

            if (polygonPoints().size > 2) {
                polygonPoints().zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = Color.Green,
                        start = start.toAbsolute(currentEditorSize),
                        end = end.toAbsolute(currentEditorSize),
                        strokeWidth = 3f
                    )
                }
            }

            if (polygonPoints().isNotEmpty()) {
                polygonPoints().forEach { point ->
                    drawCircle(color = SelectedItemColor, center = point.toAbsolute(currentEditorSize), radius = 8f)
                }
            }
        }

        /*Box(
            Modifier
                .matchParentSize()
                .zIndex(1f)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            var index = 0
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            index++
                            val count = event.changes.count { it.pressed }
                            if (count in 0..1 && index == 1) continue
                            fingerCount = count
                            timber("CustomTransformable", "Global observer: $fingerCount")
                        }
                    }
                }
                .pointerInteropFilter { false }
        )*/

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
    currentCursor: () -> CursorData,
    editorSize: () -> IntSize,
    onMainUiEvent: (MainUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.TopStart
    ) {
        if (overlays.isNotEmpty()) {
            overlays.forEach { overlayData ->
                Overlay(overlayData, currentCursor, editorSize, onMainUiEvent)
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
    currentCursor: () -> CursorData,
    editorSize: () -> IntSize,
    onMainUiEvent: (MainUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPanMode = currentCursor().type == CursorTypes.DRAG_PAN

    val allowTransform = remember { mutableStateOf(false) }

    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        if (!isPanMode || !allowTransform.value) return@rememberTransformableState

        timber("CustomTransformable", "state changing")

        onMainUiEvent(
            MainUiEvent.UpdateCurrentOverlayTransform(
                zoomChange,
                offsetChange.toRelative(editorSize()),
                rotationChange
            )
        )
    }

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
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val isValid = event.changes.count { it.pressed } >= 2 && event.changes.none { it.isConsumed }
                            timber("CustomTransformable", "isValid=$isValid")
                            if (allowTransform.value != isValid) allowTransform.value = isValid
                        }
                    }
                }
                .pointerInteropFilter { false }
                .transformable(state)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            // Detect if all pointers are up (finger(s) lifted)
                            if (event.changes.all { it.changedToUpIgnoreConsumed() }) {
                                // Call your gesture end logic here
                                onMainUiEvent(MainUiEvent.SaveCurrentState)
                            }
                        }
                    }
                }
                .graphicsLayer {
                    val pivotX = overlayData.polygonCenter.x - overlayData.offset.x
                    val pivotY = overlayData.polygonCenter.y - overlayData.offset.y
                    translationX = overlayData.offset.x
                    translationY = overlayData.offset.y
                    scaleX = overlayData.scale
                    scaleY = overlayData.scale
                    rotationZ = overlayData.rotation
                    transformOrigin = TransformOrigin(
                        pivotX / overlayData.overlay.width,
                        pivotY / overlayData.overlay.height
                    )
                },
            contentScale = ContentScale.None
        )
    }
}
