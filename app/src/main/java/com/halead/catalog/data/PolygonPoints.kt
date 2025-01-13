package com.halead.catalog.data

import android.os.Parcelable
import androidx.compose.ui.geometry.Offset
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PolygonPoints(
    var polygonPoints: List<@RawValue Offset>
) : Parcelable
