package com.halead.catalog.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset

fun getClippedMaterial(
    materialBitmap: Bitmap,
    regionPoints: List<Offset>
): Bitmap {
    // Calculate the bounding box of the polygon
    val minX = regionPoints.minOf { it.x }
    val minY = regionPoints.minOf { it.y }
    val maxX = regionPoints.maxOf { it.x }
    val maxY = regionPoints.maxOf { it.y }

    val polygonWidth = maxX - minX
    val polygonHeight = maxY - minY

    // Create a blank bitmap for the output
    val resultBitmap = Bitmap.createBitmap(
        polygonWidth.toInt(),
        polygonHeight.toInt(),
        Bitmap.Config.ARGB_8888
    )

    // Canvas to draw on the result bitmap
    val canvas = Canvas(resultBitmap)

    // Create a path for the polygon region
    val polygonPath = Path().apply {
        moveTo(regionPoints[0].x - minX, regionPoints[0].y - minY) // Adjust points relative to bounding box
        for (i in 1 until regionPoints.size) {
            lineTo(regionPoints[i].x - minX, regionPoints[i].y - minY)
        }
        close()
    }

    // Clip the canvas to the polygon path
    canvas.clipPath(polygonPath)

    // Resize the material bitmap to fit the bounding box
    val resizedMaterial = Bitmap.createScaledBitmap(
        materialBitmap,
        polygonWidth.toInt(),
        polygonHeight.toInt(),
        true
    )

    // Draw the resized material on the canvas
    canvas.drawBitmap(resizedMaterial, 0f, 0f, Paint())

    // Return the resulting bitmap containing the clipped material
    return resultBitmap
}

