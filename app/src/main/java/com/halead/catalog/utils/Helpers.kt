package com.halead.catalog.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

fun findMinOffset(polygonPoints: List<Offset>): Offset {
    val minX = polygonPoints.minOfOrNull { it.x } ?: 0f
    val minY = polygonPoints.minOfOrNull { it.y } ?: 0f
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

fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}
