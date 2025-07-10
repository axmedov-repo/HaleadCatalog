package com.halead.catalog.repository.main

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getMaterials(): Flow<Result<List<Int>>>

    /**
     * Not Implemented
     */
    suspend fun addMaterial(bitmap: Bitmap?)
    suspend fun getBitmap(imageResId: Int): Bitmap?
}