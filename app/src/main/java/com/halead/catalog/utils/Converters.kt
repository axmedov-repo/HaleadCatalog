package com.halead.catalog.utils

import androidx.compose.ui.geometry.Offset
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.halead.catalog.data.room.OverlayMaterialRoomEntity

class Converters {

    private val gson = Gson()

    // Convert Offset to String
    @TypeConverter
    fun offsetToString(offset: Offset): String {
        return "${offset.x},${offset.y}"
    }

    // Convert String to Offset
    @TypeConverter
    fun stringToOffset(value: String): Offset {
        val (x, y) = value.split(",").map { it.toFloat() }
        return Offset(x, y)
    }

    // Convert List<OverlayMaterialRoomEntity> to String
    @TypeConverter
    fun listToString(list: List<OverlayMaterialRoomEntity>): String {
        return gson.toJson(list)
    }

    // Convert String to List<OverlayMaterialRoomEntity>
    @TypeConverter
    fun stringToList(value: String): List<OverlayMaterialRoomEntity> {
        val type = object : TypeToken<List<OverlayMaterialRoomEntity>>() {}.type
        return gson.fromJson(value, type)
    }
}
