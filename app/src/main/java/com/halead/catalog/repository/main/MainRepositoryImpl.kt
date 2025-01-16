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
    override fun getMaterials(): Flow<Result<List<Int>>> = flow {
        timber("Materials", "repo getMaterials()")
        emit(
            Result.success(
                dataProvider.materialsResIds
            )
        )
    }

    override fun getBitmap(imageResId: Int): Flow<Result<Bitmap?>> = flow {
        timber("Materials", "repo getBitmap() imageRes=$imageResId")
        emit(
            Result.success(
                bitmapCacheManager.getBitmap(imageResId)
            )
        )
    }
}
