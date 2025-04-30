package com.halead.catalog.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.util.Stack

suspend fun findMinOffset(regionPoints: List<Offset>): Offset = coroutineScope {
    val minX = async { regionPoints.minOf { it.x } }
    val minY = async { regionPoints.minOf { it.y } }
    return@coroutineScope Offset(minX.await(), minY.await())
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

fun getAspectRatioFromResource(resourceId: Int, context: Context): Float {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true // Only decode the dimensions
    }
    BitmapFactory.decodeResource(context.resources, resourceId, options)
    return if (options.outWidth > 0 && options.outHeight > 0) {
        options.outWidth.toFloat() / options.outHeight.toFloat()
    } else {
        1f // Default aspect ratio if dimensions are invalid
    }
}

fun timber(tag: String = "TTT", message: String) {
    Timber.tag(tag).d(message)
}

fun timberE(message: String) {
    Timber.e(message)
}

fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun <T> Stack<T>.peekOrNull(): T? {
    return if (this.isEmpty()) null else this.peek()
}

fun Bitmap.isPanoramic(): Boolean {
    val aspectRatio = this.width.toFloat() / this.height.toFloat()
    timber("isPanoramic", "${aspectRatio > 2.0}")
    return aspectRatio > 2.0  // Consider it panoramic if width is more than 2x height
}

fun findPolygonCenter(points: List<Offset>): Offset {
    val sumX = points.sumOf { it.x.toDouble() }
    val sumY = points.sumOf { it.y.toDouble() }
    return Offset((sumX / points.size).toFloat(), (sumY / points.size).toFloat())
}

fun List<Offset>.canMakeClosedShape(): Boolean = this.size >= 3
fun List<Offset>.isQuadrilateral(): Boolean = this.size == 4

@Composable
fun Modifier.noRippleClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    return this.clickable(
        enabled = enabled,
        indication = null, // Disable ripple effect
        interactionSource = remember { MutableInteractionSource() } // Required for clickable
    ) {
        onClick()
    }
}