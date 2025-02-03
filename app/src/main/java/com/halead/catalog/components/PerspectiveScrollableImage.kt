package com.halead.catalog.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.halead.catalog.utils.toPerspectiveMaterial

@Composable
fun PerspectiveScrollableImage(
    bitmap: Bitmap,
    outerPolygon: List<Offset>,
    holes: List<List<Offset>> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Create and remember the perspective material
    val perspectiveMaterial = remember(bitmap) {
        bitmap.toPerspectiveMaterial(outerPolygon, holes)
    }

    // State to track the scrolled bitmap
    var scrolledBitmap by remember {
        mutableStateOf(perspectiveMaterial.scroll(0f, 0f))
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            perspectiveMaterial.recycle()
            scrolledBitmap.recycle()
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // Apply scroll and update bitmap
                    val newBitmap = perspectiveMaterial.scroll(
                        dx = dragAmount.x,
                        dy = dragAmount.y
                    )

                    // Recycle previous bitmap before updating
                    scrolledBitmap.recycle()
                    scrolledBitmap = newBitmap
                }
            }
    ) {
        // Convert bitmap to Compose Image
        Image(
            bitmap = scrolledBitmap.asImageBitmap(),
            contentDescription = "Perspective Scrollable Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.None
        )
    }
}