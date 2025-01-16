package com.halead.catalog.data.room

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OverlayMaterialRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val overlayFilePath: String,
    val material : Int, // because of it is static image currently
    val regionPoints: List<Offset>,
    var position: Offset = Offset(0f, 0f)
)
