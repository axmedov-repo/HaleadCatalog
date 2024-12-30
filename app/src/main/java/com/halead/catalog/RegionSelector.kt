package com.halead.catalog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun RegionSelector(
    imageBitmap: ImageBitmap,
    isMaterialApplied: Boolean,
    onDrawingStarted: () -> Unit,
    onRegionSelected: (Offset, Offset) -> Unit
) {
    var startPoint by remember { mutableStateOf<Offset?>(null) }
    var endPoint by remember { mutableStateOf<Offset?>(null) }

    var imageOffsetX by remember { mutableFloatStateOf(0f) }
    var imageOffsetY by remember { mutableFloatStateOf(0f) }
    var imageScale by remember { mutableFloatStateOf(1f) }
    val aspectRatio = imageBitmap.width.toFloat() / imageBitmap.height.toFloat()

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    if (isWithinImageBounds(offset, imageOffsetX, imageOffsetY, imageBitmap, imageScale)) {
                        onDrawingStarted()
                        startPoint = adjustToImageCoordinates(offset, imageOffsetX, imageOffsetY, imageScale)
                        endPoint = startPoint
                    }
                },
                onDrag = { _, dragAmount ->
                    if (startPoint != null) {
                        val newEnd = endPoint?.let {
                            Offset(it.x + dragAmount.x / imageScale, it.y + dragAmount.y / imageScale)
                        }
                        if (newEnd != null) {
                            endPoint = newEnd
                        }
                    }
                },
                onDragEnd = {
                    if (startPoint != null && endPoint != null) {
                        val regionStart = startPoint!!.clampToImage(imageBitmap)
                        val regionEnd = endPoint!!.clampToImage(imageBitmap)
                        onRegionSelected(regionStart, regionEnd)
                    }
                }
            )
        }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val imageWidth = imageBitmap.width.toFloat()
        val imageHeight = imageBitmap.height.toFloat()

        if (imageWidth > 0 && imageHeight > 0) {
            // Calculate the scale factor and offsets for centering the image
            imageScale = minOf(canvasWidth / imageWidth, canvasHeight / imageHeight)

            val scaledImageWidth = imageWidth * imageScale
            val scaledImageHeight = imageHeight * imageScale

            imageOffsetX = (canvasWidth - scaledImageWidth) / 2
            imageOffsetY = (canvasHeight - scaledImageHeight) / 2

            // Draw the scaled and centered image
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val scaledBitmap = imageBitmap.asAndroidBitmap()
                nativeCanvas.drawBitmap(
                    scaledBitmap,
                    null,
                    android.graphics.RectF(
                        imageOffsetX,
                        imageOffsetY,
                        imageOffsetX + scaledImageWidth,
                        imageOffsetY + scaledImageHeight
                    ),
                    null
                )
            }
        }

        // Draw the selection rectangle in screen coordinates
        if (!isMaterialApplied) {
            startPoint?.let { start ->
                endPoint?.let { end ->
                    // Convert image coordinates back to screen space
                    val screenStart = adjustToScreenCoordinates(start, imageOffsetX, imageOffsetY, imageScale)
                    val screenEnd = adjustToScreenCoordinates(end, imageOffsetX, imageOffsetY, imageScale)

                    drawRect(
                        color = Color.Green.copy(alpha = 0.5f),
                        topLeft = Offset(
                            x = minOf(screenStart.x, screenEnd.x),
                            y = minOf(screenStart.y, screenEnd.y)
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            width = kotlin.math.abs(screenStart.x - screenEnd.x),
                            height = kotlin.math.abs(screenStart.y - screenEnd.y)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Adjusts screen coordinates to match image coordinates based on scaling and offsets.
 */
private fun adjustToImageCoordinates(
    screenOffset: Offset,
    imageOffsetX: Float,
    imageOffsetY: Float,
    imageScale: Float
): Offset {
    val adjustedX = (screenOffset.x - imageOffsetX) / imageScale
    val adjustedY = (screenOffset.y - imageOffsetY) / imageScale
    return Offset(adjustedX, adjustedY)
}

/**
 * Adjusts image coordinates to match screen coordinates based on scaling and offsets.
 */
private fun adjustToScreenCoordinates(
    imageOffset: Offset,
    imageOffsetX: Float,
    imageOffsetY: Float,
    imageScale: Float
): Offset {
    val screenX = imageOffset.x * imageScale + imageOffsetX
    val screenY = imageOffset.y * imageScale + imageOffsetY
    return Offset(screenX, screenY)
}

/**
 * Clamps the offset coordinates to ensure they are within the image bounds.
 */
private fun Offset.clampToImage(imageBitmap: ImageBitmap): Offset {
    val maxWidth = imageBitmap.width.toFloat()
    val maxHeight = imageBitmap.height.toFloat()
    return Offset(
        x = x.coerceIn(0f, maxWidth),
        y = y.coerceIn(0f, maxHeight)
    )
}

/**
 * Checks if a given screen offset is within the bounds of the displayed image.
 */
private fun isWithinImageBounds(
    offset: Offset,
    imageOffsetX: Float,
    imageOffsetY: Float,
    imageBitmap: ImageBitmap,
    imageScale: Float
): Boolean {
    val imageWidth = imageBitmap.width * imageScale
    val imageHeight = imageBitmap.height * imageScale

    return offset.x >= imageOffsetX &&
            offset.x <= imageOffsetX + imageWidth &&
            offset.y >= imageOffsetY &&
            offset.y <= imageOffsetY + imageHeight
}
