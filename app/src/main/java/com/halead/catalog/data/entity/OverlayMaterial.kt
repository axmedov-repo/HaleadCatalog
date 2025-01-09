package com.halead.catalog.data.entity

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset

data class OverlayMaterial(
    val materialBitmap: Bitmap,
    val regionPoints: List<Offset>,
    var position: Offset = Offset(0f, 0f)
)
