package com.halead.catalog.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.halead.catalog.ui.theme.ButtonColor
import kotlinx.coroutines.coroutineScope
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

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

/*suspend fun getClippedMaterial(
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
        holes.forEach {
            val hole = clipPolygon(it, outerPolygon)
            if (hole.isNotEmpty() && hole.all { holePoint -> isPointInPolygon(holePoint, outerPolygon) }) {
                moveTo(hole[0].x - minX, hole[0].y - minY)
                for (i in hole.size - 1 downTo 1) {
                    lineTo(hole[i].x - minX, hole[i].y - minY)
                }
                close()
            }
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
}*/

suspend fun getClippedMaterial(
    materialBitmap: Bitmap,
    outerPolygon: List<Offset>,
    holes: List<List<Offset>> = emptyList()
): Bitmap = coroutineScope {
    // Validate polygon points
    if (!outerPolygon.canMakeClosedShape()) return@coroutineScope Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    // Calculate polygon bounds
    val minX = outerPolygon.minOf { it.x }
    val minY = outerPolygon.minOf { it.y }
    val maxX = outerPolygon.maxOf { it.x }
    val maxY = outerPolygon.maxOf { it.y }

    val width = (maxX - minX).toInt()
    val height = (maxY - minY).toInt()

    // Convert material bitmap
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)

    // Ensure material size matches expected bounding box
    val resizedMaterial = Mat()
    Imgproc.resize(materialMat, resizedMaterial, Size(width.toDouble(), height.toDouble()))

    // Get number of channels in material
    val materialType = resizedMaterial.type()  // CV_8UC3 or CV_8UC4 (RGB/ARGB)
    val materialChannels = resizedMaterial.channels()  // 3 or 4

    // Create a blank mask
    val mask = Mat(height, width, CvType.CV_8UC1, Scalar(0.0))

    // Convert outer polygon to MatOfPoint
    val outerContour = MatOfPoint()
    outerContour.fromList(outerPolygon.map {
        Point((it.x - minX).toDouble(), (it.y - minY).toDouble())
    })

    // Create list of all contours (outer + holes)
    val contours = mutableListOf<MatOfPoint>()
    contours.add(outerContour)  // Add outer contour first

    // Add hole contours
    holes.forEach { hole ->
        val holeContour = MatOfPoint()
        holeContour.fromList(hole.map {
            Point((it.x - minX).toDouble(), (it.y - minY).toDouble())
        })
        contours.add(holeContour)
    }

    // Create hierarchy for contours
    val hierarchy = Mat()

    // Draw outer contour in white
    Imgproc.drawContours(mask, contours, 0, Scalar(255.0), Imgproc.FILLED, 8, hierarchy, 0)

    // Draw holes in black
    for (i in 1 until contours.size) {
        Imgproc.drawContours(mask, contours, i, Scalar(0.0), Imgproc.FILLED, 8, hierarchy, 0)
    }

    // Convert mask to match the number of material channels (CV_8UC3 or CV_8UC4)
    var maskChannels = Mat()
    if (materialChannels > 1) {
        Core.merge(List(materialChannels) { mask }, maskChannels) // Duplicate channels
    } else {
        maskChannels = mask
    }

    // Apply mask to material
    val result = Mat()
    Core.bitwise_and(resizedMaterial, maskChannels, result)

    // Convert back to bitmap
    val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(result, resultBitmap)

    // Resource cleanup
    materialMat.release()
    resizedMaterial.release()
    mask.release()
    maskChannels.release()
    result.release()
    outerContour.release()
    contours.forEach { it.release() }
    hierarchy.release()

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
