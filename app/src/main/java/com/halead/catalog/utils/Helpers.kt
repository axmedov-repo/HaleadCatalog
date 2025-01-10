package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun saveImageToFile(context: Context, bitmap: Bitmap, fileName: String): String {
    val fileDir = context.filesDir
    val file = File(fileDir, fileName)

    FileOutputStream(file).use { fos ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    }

    return file.absolutePath
}
