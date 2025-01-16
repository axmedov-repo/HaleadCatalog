package com.halead.catalog.repository.main

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    fun getMaterials(): Flow<Result<List<Int>>>
    fun getBitmap(imageResId: Int): Flow<Result<Bitmap?>>
}