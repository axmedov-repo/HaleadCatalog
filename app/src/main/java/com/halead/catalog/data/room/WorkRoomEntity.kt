package com.halead.catalog.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WorkRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val baseImageFilePath: String,
    val overlays: List<OverlayMaterialRoomEntity>
)
