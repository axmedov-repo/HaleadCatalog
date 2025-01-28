package com.halead.catalog.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/**
 * Applying material with perspective to only quadrilateral shapes which has only 4 vertices.
 */

fun applyMaterialToQuadrilateral(
    materialBitmap: Bitmap,
    polygonPoints: List<Offset>
): Bitmap {
    if (polygonPoints.size < 3) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    // Calculate bounding box
    val minX = polygonPoints.minOf { it.x }
    val minY = polygonPoints.minOf { it.y }
    val maxX = polygonPoints.maxOf { it.x }
    val maxY = polygonPoints.maxOf { it.y }

    val width = maxX - minX
    val height = maxY - minY

    if (width <= 0 || height <= 0) {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    // Create source points from material bitmap
    val srcPoints = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(materialBitmap.width.toDouble(), 0.0),
        Point(materialBitmap.width.toDouble(), materialBitmap.height.toDouble()),
        Point(0.0, materialBitmap.height.toDouble())
    )

    // Create destination points that exactly match the target polygon
    val dstPoints = MatOfPoint2f().apply {
        fromList(polygonPoints.mapIndexed { index, point ->
            Point(
                (point.x - minX).toDouble(),
                (point.y - minY).toDouble()
            )
        })
    }

    // Calculate transformation matrix
    val transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

    // Convert material bitmap to Mat
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)

    // Create output Mat and apply transformation
    val outputMat = Mat()
    Imgproc.warpPerspective(
        materialMat,
        outputMat,
        transformMatrix,
        Size(width.toDouble(), height.toDouble()),
        Imgproc.INTER_LINEAR
    )

    // Convert result back to Bitmap
    val resultBitmap = Bitmap.createBitmap(
        width.toInt(),
        height.toInt(),
        Bitmap.Config.ARGB_8888
    )
    Utils.matToBitmap(outputMat, resultBitmap)

    // Clean up
    materialMat.release()
    outputMat.release()
    transformMatrix.release()
    srcPoints.release()
    dstPoints.release()

    return resultBitmap
}
