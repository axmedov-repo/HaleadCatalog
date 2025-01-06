package com.halead.catalog.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

fun createMaterialForRegion(
    baseImage: Bitmap,
    materialBitmap: Bitmap,
    regionPoints: List<Offset>
): Bitmap {
    // Convert baseImage to Mat
    val matImage = Mat()
    Utils.bitmapToMat(baseImage, matImage)

    // Create a mask for the region
    val mask = Mat.zeros(matImage.size(), CvType.CV_8UC1)
    val scaledPoints = regionPoints.map { org.opencv.core.Point(it.x.toDouble(), it.y.toDouble()) }
    val matOfPoint = MatOfPoint()
    matOfPoint.fromList(scaledPoints)
    Imgproc.fillPoly(mask, listOf(matOfPoint), Scalar(255.0))

    // Convert materialBitmap to Mat
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)

    // Calculate the bounding box of the region
    val boundingRect = Imgproc.boundingRect(matOfPoint)

    // Resize material to fit the bounding box
    val materialSize = org.opencv.core.Size(boundingRect.width.toDouble(), boundingRect.height.toDouble())
    val resizedMaterial = Mat()
    Imgproc.resize(materialMat, resizedMaterial, materialSize)

    // Create a region of interest (ROI) for the selected region on the base image
    val regionROI = Mat(matImage, boundingRect)

    // Copy the resized material into the selected region using the mask
    resizedMaterial.copyTo(regionROI, mask)

    // Convert the result back to Bitmap
    val resultBitmap = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(matImage, resultBitmap)

    // Release resources
    matImage.release()
    mask.release()
    materialMat.release()
    resizedMaterial.release()

    return resultBitmap
}

fun drawMaterialOnPolygon(
    baseImage: Bitmap,
    materialBitmap: Bitmap,
    regionPoints: List<Offset>
): Bitmap {
    // Create a mutable bitmap for the result
    val resultBitmap = Bitmap.createBitmap(baseImage.width, baseImage.height, Bitmap.Config.ARGB_8888)

    // Create a canvas to draw on the result bitmap
    val canvas = Canvas(resultBitmap)

    // Draw the base image on the canvas
    canvas.drawBitmap(baseImage, 0f, 0f, null)

    // Create a path for the polygon based on the region points
    val path = Path()
    val firstPoint = regionPoints[0]
    path.moveTo(firstPoint.x, firstPoint.y)

    for (i in 1 until regionPoints.size) {
        val point = regionPoints[i]
        path.lineTo(point.x, point.y)
    }

    // Close the path to form a complete polygon
    path.close()

    // Get the bounding box of the polygon
    val minX = regionPoints.minOf { it.x }
    val minY = regionPoints.minOf { it.y }
    val maxX = regionPoints.maxOf { it.x }
    val maxY = regionPoints.maxOf { it.y }

    val polygonWidth = maxX - minX
    val polygonHeight = maxY - minY

    // Resize the material bitmap to fit the polygon's bounding box
    val matrix = Matrix()
    matrix.setRectToRect(
        RectF(0f, 0f, materialBitmap.width.toFloat(), materialBitmap.height.toFloat()),
        RectF(0f, 0f, polygonWidth, polygonHeight),
        Matrix.ScaleToFit.FILL
    )

    val resizedMaterial = Bitmap.createBitmap(
        materialBitmap, 0, 0, materialBitmap.width, materialBitmap.height, matrix, true
    )

    // Create a temporary canvas to draw the resized material
    val tempCanvas = Canvas(resizedMaterial)

    // Clip the canvas to the polygon path
    tempCanvas.clipPath(path)

    // Draw the resized material onto the clipped canvas
    tempCanvas.drawBitmap(resizedMaterial, 0f, 0f, Paint())

    // Now apply the material to the base image using the polygon's bounds
    canvas.drawBitmap(resizedMaterial, minX, minY, null)

    // Return the final result bitmap
    return resultBitmap
}


