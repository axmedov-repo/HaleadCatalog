package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

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


fun getBitmapFromResource(context: Context, resourceId: Int?): Bitmap? {
    return resourceId?.let { BitmapFactory.decodeResource(context.resources, it) }
}
