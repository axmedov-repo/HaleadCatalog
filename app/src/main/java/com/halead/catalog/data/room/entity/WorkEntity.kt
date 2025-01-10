package com.halead.catalog.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.halead.catalog.data.models.OverlayMaterial

@Entity
data class WorkEntity(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val baseImageResName : String,
    val overlays : List<OverlayMaterial>
)
