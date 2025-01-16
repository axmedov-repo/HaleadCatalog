package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.compose.ui.geometry.Offset

fun applyMaterialToBaseImage(
    baseImage: Bitmap,
    materialBitmap: Bitmap,
    regionPoints: List<Offset>,
    offset: Offset
): Bitmap {
    require(regionPoints.size >= 3) { "regionPoints must contain at least 3 points." }

    val resultBitmap = baseImage.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(resultBitmap)

    val path = Path().apply {
        moveTo(regionPoints[0].x, regionPoints[0].y)
        for (i in 1 until regionPoints.size) {
            lineTo(regionPoints[i].x, regionPoints[i].y)
        }
        close()
    }

    val minX = regionPoints.minOf { it.x }
    val minY = regionPoints.minOf { it.y }
    val maxX = regionPoints.maxOf { it.x }
    val maxY = regionPoints.maxOf { it.y }

    val regionWidth = (maxX - minX).toFloat()
    val regionHeight = (maxY - minY).toFloat()

    // Calculate scaling factors to fit region size
    val scaleX = regionWidth / materialBitmap.width
    val scaleY = regionHeight / materialBitmap.height

    // Create a matrix for scaling and translation
    val matrix = Matrix().apply {
        postScale(scaleX, scaleY)
        postTranslate(minX + offset.x, minY + offset.y)
    }

    // Create a cropped and scaled material bitmap
    val croppedMaterialBitmap = Bitmap.createBitmap(
        materialBitmap,
        0,
        0,
        materialBitmap.width,
        materialBitmap.height,
        matrix,
        true
    )

    val saveCount = canvas.save()

    // Clip the canvas to the custom path
    canvas.clipPath(path)

    // Draw the cropped and scaled material bitmap
    canvas.drawBitmap(croppedMaterialBitmap, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))

    canvas.restoreToCount(saveCount)

    return resultBitmap
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

fun getDownscaledBitmapFromResource(context: Context, resourceId: Int): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true // Load dimensions only
    }

    BitmapFactory.decodeResource(context.resources, resourceId, options)

    options.inSampleSize = calculateInSampleSize(options, 512, 512) // Resize to 512x512
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeResource(context.resources, resourceId, options)
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
