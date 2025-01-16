package com.halead.catalog.data.cache

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BitmapCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Preload drawable resources into Glide's cache
    suspend fun cacheLocalImages(imageResourceIds: List<Int>) = coroutineScope {
        imageResourceIds.map { drawableId ->
            async(Dispatchers.IO) {
                Glide.with(context)
                    .load(drawableId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload()
            }
        }.awaitAll() // Wait for all coroutines to complete
    }

    // Load bitmap from URL (uses Glide’s caching for memory and disk)
    suspend fun getOrCacheRemoteImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache remote images
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Load bitmap from local resources (uses Glide’s memory cache)
    suspend fun getBitmap(resourceId: Int): Bitmap? = withContext(Dispatchers.IO) {
        try {
            Glide.with(context)
                .asBitmap()
                .load(resourceId)
                .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Clear Glide’s cache (optional utility function)
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        Glide.get(context).clearMemory()
        Glide.get(context).clearDiskCache()
    }
}
