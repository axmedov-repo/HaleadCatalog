package com.halead.catalog.utils

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.scale

fun Offset.toRelative(relativeSize: IntSize): Offset {
    return Offset((this.x / relativeSize.width) * 1000, (this.y / relativeSize.height) * 1000)
}

fun Offset.toAbsolute(relativeSize: IntSize): Offset {
    return Offset((this.x * relativeSize.width) / 1000, (this.y * relativeSize.height) / 1000)
}

fun Bitmap.toAbsolute(relativeSize: IntSize): Bitmap {
    return this.scale((this.width * relativeSize.width) / 1000, (this.height * relativeSize.height) / 1000)
}