package com.halead.catalog.data.models

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset

data class OverlayMaterialModel(
    val id: Int = 0, // for work history with OverlayMaterialEntity
    val overlay: Bitmap,
    val material : Int,
    val regionPoints: List<Offset>,
    var position: Offset = Offset(0f, 0f)
)
