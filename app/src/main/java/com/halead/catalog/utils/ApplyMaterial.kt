package com.halead.catalog.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.halead.catalog.ui.theme.ButtonColor
import kotlinx.coroutines.coroutineScope

/*suspend fun getClippedMaterial(
    materialBitmap: Bitmap,
    regionPoints: List<Offset>
): Bitmap = coroutineScope {
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
    return@coroutineScope resultBitmap
}*/

suspend fun getClippedMaterial(
    materialBitmap: Bitmap,
    outerPolygon: List<Offset>,
    holes: List<List<Offset>> = emptyList()
): Bitmap = coroutineScope {
    // Validate polygon points
    if (outerPolygon.size < 3) return@coroutineScope Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    // Calculate the bounding box of the polygon
    val minX = outerPolygon.minOf { it.x }
    val minY = outerPolygon.minOf { it.y }
    val maxX = outerPolygon.maxOf { it.x }
    val maxY = outerPolygon.maxOf { it.y }

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

    // Create a path for the outer polygon region
    val polygonPath = Path().apply {
        // Add outer polygon
        moveTo(outerPolygon[0].x - minX, outerPolygon[0].y - minY)
        for (i in 1 until outerPolygon.size) {
            lineTo(outerPolygon[i].x - minX, outerPolygon[i].y - minY)
        }
        close()

        // Add holes using counter-clockwise winding
        holes.forEach { hole ->
            moveTo(hole[0].x - minX, hole[0].y - minY)
            for (i in hole.size - 1 downTo 1) {
                lineTo(hole[i].x - minX, hole[i].y - minY)
            }
            close()
        }

        // Set fill type to handle holes correctly
        fillType = Path.FillType.EVEN_ODD
    }

    // Clip the canvas to the polygon path with holes
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

    // Clean up the resized bitmap
    if (resizedMaterial != materialBitmap) {
        resizedMaterial.recycle()
    }

    return@coroutineScope resultBitmap
}

suspend fun getTemporaryClippedOverlay(
    regionPoints: List<Offset>
): Bitmap = coroutineScope {
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
    val customMaterial = IntArray(polygonWidth.toInt() * polygonHeight.toInt()) { ButtonColor.copy(0.3f).toArgb() }
    val resizedMaterial = Bitmap.createScaledBitmap(
        Bitmap.createBitmap(customMaterial, polygonWidth.toInt(), polygonHeight.toInt(), Bitmap.Config.ARGB_8888),
        polygonWidth.toInt(),
        polygonHeight.toInt(),
        true
    )

    // Draw the resized material on the canvas
    canvas.drawBitmap(resizedMaterial, 0f, 0f, Paint())

    // Return the resulting bitmap containing the clipped material
    return@coroutineScope resultBitmap
}
