package com.halead.catalog.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
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

fun applyMaterialToPolygon(
    materialBitmap: Bitmap,
    polygonPoints: List<Offset>
): Bitmap {
    // Validate polygon points
    if (polygonPoints.size < 3) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    // Find minimum bounding rotated rectangle
    val points = polygonPoints.map { Point(it.x.toDouble(), it.y.toDouble()) }
    val matOfPoints = MatOfPoint2f(*points.toTypedArray())
    val rotatedRect = Imgproc.minAreaRect(matOfPoints)

    // Get rectangle corners
    val rectCorners = getPolygonHull(polygonPoints)//Array(4) { Point() }
//        rotatedRect.points(rectCorners)

    // Calculate polygon bounds
    val minX = polygonPoints.minOf { it.x }
    val minY = polygonPoints.minOf { it.y }
    val maxX = polygonPoints.maxOf { it.x }
    val maxY = polygonPoints.maxOf { it.y }

    val width = (maxX - minX).toInt()
    val height = (maxY - minY).toInt()

    // Source points from material
    val srcPoints = MatOfPoint2f(
        Point(0.0, 0.0),
        Point(materialBitmap.width.toDouble(), 0.0),
        Point(materialBitmap.width.toDouble(), materialBitmap.height.toDouble()),
        Point(0.0, materialBitmap.height.toDouble())
    )

    // Destination points adjusted to polygon coordinate system
    val dstPoints = MatOfPoint2f(
        Point((rectCorners[0].x - minX).toDouble(), (rectCorners[0].y - minY).toDouble()),
        Point((rectCorners[1].x - minX).toDouble(), (rectCorners[1].y - minY).toDouble()),
        Point((rectCorners[2].x - minX).toDouble(), (rectCorners[2].y - minY).toDouble()),
        Point((rectCorners[3].x - minX).toDouble(), (rectCorners[3].y - minY).toDouble())
    )

    // Perspective transformation
    val transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

    // Convert material bitmap
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)

    // Apply perspective transformation
    val outputMat = Mat()
    Imgproc.warpPerspective(
        materialMat,
        outputMat,
        transformMatrix,
        Size(width.toDouble(), height.toDouble()),
        Imgproc.INTER_LINEAR
    )

    // Create polygon mask
    val mask = Mat(height, width, CvType.CV_8UC1, Scalar(0.0))
    val polygonMat = MatOfPoint()
    polygonMat.fromList(polygonPoints.map {
        Point((it.x - minX).toDouble(), (it.y - minY).toDouble())
    })
    val polygons = ArrayList<MatOfPoint>()
    polygons.add(polygonMat)
    Imgproc.fillPoly(mask, polygons, Scalar(255.0))

    // Apply mask to transformed material
    val result = Mat()
    Core.bitwise_and(outputMat, outputMat, result, mask)

    // Convert back to bitmap
    val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(result, resultBitmap)

    // Resource cleanup
    materialMat.release()
    outputMat.release()
    transformMatrix.release()
    srcPoints.release()
    dstPoints.release()
    mask.release()
    polygonMat.release()
    result.release()

    return resultBitmap
}