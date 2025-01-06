package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

fun applyMaterialToPolygon(
    bitmap: Bitmap,
    polygonPoints: List<Offset>,
    materialBitmap: Bitmap?
): Bitmap {
    if (materialBitmap == null) return bitmap

    // Convert the input bitmap to Mat format
    val matImage = Mat()
    Utils.bitmapToMat(bitmap, matImage)

    // Calculate the bounding box for the selected region (polygon)
    val minX = polygonPoints.minOf { it.x }.toInt()
    val maxX = polygonPoints.maxOf { it.x }.toInt()
    val minY = polygonPoints.minOf { it.y }.toInt()
    val maxY = polygonPoints.maxOf { it.y }.toInt()

    // Calculate the width and height of the selected area
    val width = maxX - minX
    val height = maxY - minY

    // Create a mask for the polygon area
    val mask = Mat.zeros(matImage.size(), CvType.CV_8UC1)

    // Convert the polygon points into OpenCV points
    val scaledPoints = polygonPoints.map { org.opencv.core.Point(it.x.toDouble(), it.y.toDouble()) }
    val matOfPoint = MatOfPoint()
    matOfPoint.fromList(scaledPoints)

    // Fill the polygon area with white (255) in the mask
    Imgproc.fillPoly(mask, listOf(matOfPoint), Scalar(255.0))

    // Ensure the mask and image have the same size
    if (mask.size() != matImage.size()) {
        Imgproc.resize(mask, mask, matImage.size())  // Resize the mask to match the image size
    }

    // Convert material bitmap to Mat format
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)

    // Repeat the material to cover the selected area
    val tiledMaterialWidth = (width.toDouble() / materialMat.width()).toInt() + 1
    val tiledMaterialHeight = (height.toDouble() / materialMat.height()).toInt() + 1

    // Create an empty canvas to hold the tiled material
    val tiledMaterial = Mat()

    // Resize material to cover the selected area
    Imgproc.resize(
        materialMat,
        tiledMaterial,
        org.opencv.core.Size(
            materialMat.width() * tiledMaterialWidth.toDouble(),
            materialMat.height() * tiledMaterialHeight.toDouble()
        )
    )

    // Crop the material to fit the selected region
    val croppedTiledMaterial = tiledMaterial.submat(
        org.opencv.core.Rect(minX, minY, width, height)
    )

    // Rotate the cropped material to match the selected region's rotation (optional)
    val rotationMatrix = Imgproc.getRotationMatrix2D(
        org.opencv.core.Point(width / 2.0, height / 2.0),  // Rotation center
        getRotationAngle(polygonPoints),                    // You need a function to get the angle based on the polygon's orientation
        1.0
    )

    val rotatedMaterial = Mat()
    Imgproc.warpAffine(croppedTiledMaterial, rotatedMaterial, rotationMatrix, croppedTiledMaterial.size())

    // Copy the rotated material to the original image using the mask
    rotatedMaterial.copyTo(matImage, mask)

    // Convert the result back to Bitmap format
    val updatedBitmap = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(matImage, updatedBitmap)

    // Release resources
    matImage.release()
    materialMat.release()
    mask.release()
    tiledMaterial.release()
    croppedTiledMaterial.release()
    rotatedMaterial.release()

    return updatedBitmap
}

fun getRotationAngle(polygonPoints: List<Offset>): Double {
    // You can calculate the angle based on the direction of one edge of the polygon or other method
    // This is just an example for simple cases
    val p1 = polygonPoints[0]
    val p2 = polygonPoints[1]
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    return Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble()))
}

fun applyMaterialToRectangle(
    bitmap: Bitmap,
    startPoint: Offset,
    endPoint: Offset,
    materialBitmap: Bitmap?
): Bitmap {
    Log.d("MATERIAL_CHANGE", "applyMaterialToRectangle to ${materialBitmap.toString()}")
    if (materialBitmap == null) return bitmap // Check for null material bitmap

    val matImage = Mat()
    Utils.bitmapToMat(bitmap, matImage)

    // Ensure rectangle bounds are within the image size
    val left = startPoint.x.toInt().coerceIn(0, bitmap.width)
    val top = startPoint.y.toInt().coerceIn(0, bitmap.height)
    val right = endPoint.x.toInt().coerceIn(0, bitmap.width)
    val bottom = endPoint.y.toInt().coerceIn(0, bitmap.height)

    // Create a mask for the rectangle
    val mask = Mat.zeros(matImage.size(), CvType.CV_8UC1)
    Imgproc.rectangle(
        mask,
        org.opencv.core.Point(left.toDouble(), top.toDouble()),
        org.opencv.core.Point(right.toDouble(), bottom.toDouble()),
        Scalar(255.0),
        -1 // Fill the rectangle
    )

    // Resize material to match mask
    val materialMat = Mat()
    Utils.bitmapToMat(materialBitmap, materialMat)
    Imgproc.resize(materialMat, materialMat, matImage.size())

    // Ensure mask and image sizes match
    if (mask.size() != matImage.size()) {
        throw IllegalArgumentException("Mask size must match the image size.")
    }

    // Apply material to the selected rectangle
    materialMat.copyTo(matImage, mask)

    // Convert the result back to a Bitmap
    val updatedBitmap = Bitmap.createBitmap(matImage.cols(), matImage.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(matImage, updatedBitmap)

    // Release resources
    matImage.release()
    materialMat.release()
    mask.release()

    Log.d("MATERIAL_CHANGE", "Returning bitmap = $updatedBitmap")
    return updatedBitmap
}

fun getBitmapFromUri(context: Context, uri: Uri?): Bitmap? {
    return uri?.let {
        context.contentResolver.openInputStream(it)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    }
}

fun getBitmapFromResource(context: Context, resourceId: Int): Bitmap? {
    return BitmapFactory.decodeResource(context.resources, resourceId)
}

fun getBitmapFromVector(context: Context, resourceId: Int): Bitmap {
    // Load the vector drawable
    val drawable: Drawable = ContextCompat.getDrawable(context, resourceId)!!

    // Check if the drawable is an instance of VectorDrawable (Lollipop and above)
    if (drawable is VectorDrawableCompat) {
        // For pre-Lollipop, use VectorDrawableCompat
        return vectorDrawableToBitmap(drawable)
    } else if (drawable is android.graphics.drawable.VectorDrawable) {
        // For Lollipop and above, use VectorDrawable
        return vectorDrawableToBitmap(drawable)
    } else {
        throw IllegalArgumentException("Unsupported drawable type")
    }
}

fun vectorDrawableToBitmap(drawable: Drawable): Bitmap {
    // Create a Bitmap with the required width and height
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

    // Create a Canvas to render the vector drawable
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
