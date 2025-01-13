package com.halead.catalog.utils

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext

fun findMinOffset(regionPoints: List<Offset>): Offset {
    val minX = regionPoints.minOf { it.x }
    val minY = regionPoints.minOf { it.y }
    return Offset(minX, minY)
}

fun getRegionSize(regionPoints: List<Offset>): Size {
    if (regionPoints.isEmpty()) {
        return Size(0f, 0f)
    }

    // Initialize min and max values with the first point
    var minX = regionPoints[0].x
    var minY = regionPoints[0].y
    var maxX = regionPoints[0].x
    var maxY = regionPoints[0].y

    // Iterate over the region points to find the min/max values
    for (point in regionPoints) {
        minX = minOf(minX, point.x)
        minY = minOf(minY, point.y)
        maxX = maxOf(maxX, point.x)
        maxY = maxOf(maxY, point.y)
    }

    // Calculate the width and height of the region
    val width = maxX - minX
    val height = maxY - minY

    return Size(width, height)
}

@Composable
fun getAspectRatioFromResource(resourceId: Int): Float {
    val context = LocalContext.current

    // Use remember to avoid recalculating on recomposition
    return remember(resourceId) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // Only decode the dimensions
        }
        BitmapFactory.decodeResource(context.resources, resourceId, options)
        if (options.outWidth > 0 && options.outHeight > 0) {
            options.outWidth.toFloat() / options.outHeight.toFloat()
        } else {
            1f // Default aspect ratio if dimensions are invalid
        }
    }
}
