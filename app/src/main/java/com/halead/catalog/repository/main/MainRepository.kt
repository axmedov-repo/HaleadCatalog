package com.halead.catalog.repository.main

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    fun getMaterials(): Flow<Result<List<Int>>>

    /**
     * Not Implemented
     */
    fun addMaterial(bitmap: Bitmap?)
    fun getBitmap(imageResId: Int): Flow<Result<Bitmap?>>
}