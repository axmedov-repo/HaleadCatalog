package com.halead.catalog.data.models

import android.graphics.Bitmap

data class WorkModel(
    val id: Int = 0,
    val baseImage: Bitmap,
    val overlays: List<OverlayMaterialModel>
)
