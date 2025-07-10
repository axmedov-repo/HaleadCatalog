package com.halead.catalog.repository.main

import android.graphics.Bitmap
import com.halead.catalog.data.DataProvider
import com.halead.catalog.data.cache.BitmapCacheManager
import com.halead.catalog.utils.timber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val dataProvider: DataProvider,
    private val bitmapCacheManager: BitmapCacheManager
) : MainRepository {
    override suspend fun getMaterials(): Flow<Result<List<Int>>> = flow {
        timber("Materials", "repo getMaterials()")
        emit(
            Result.success(
                dataProvider.materialsResIds
            )
        )
    }

    /**
     * Not Implemented
     */
    override suspend fun addMaterial(bitmap: Bitmap?) {}

    override suspend fun getBitmap(imageResId: Int): Bitmap? {
        timber("Materials", "repo getBitmap() imageRes=$imageResId")
        return bitmapCacheManager.getBitmap(imageResId)
    }
}
