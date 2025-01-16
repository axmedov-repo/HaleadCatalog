package com.halead.catalog.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.halead.catalog.utils.getBitmapFromResource
import com.halead.catalog.utils.timber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManualBitmapCacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bitmapCache: LruCache<String, Bitmap>
    private val diskCacheDir: File

    // Initialize the manager
    init {
        // Initialize memory cache
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 4
        bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 1024 // Size in KB
            }
        }

        // Set up disk cache directory
        diskCacheDir = File(context.cacheDir, "bitmap_cache")
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
    }

    // Preload drawable resources into memory cache
    suspend fun cacheLocalImages(imageResourceIds: List<Int>) = coroutineScope {
        imageResourceIds.map { drawableId ->
            launch(Dispatchers.IO) {
                if (bitmapCache.get(drawableId.toString()) == null) {
                    val bitmap = decodeBitmapFromDrawable(drawableId)
                    bitmap?.let {
                        bitmapCache.put(drawableId.toString(), it)
                    }
                }
            }
        }.forEach { it.join() } // Ensure all launches complete before returning
    }

    // Decode bitmap from drawable resources
    private suspend fun decodeBitmapFromDrawable(drawableId: Int): Bitmap? = coroutineScope {
        return@coroutineScope try {
//            val options = BitmapFactory.Options().apply {
//                inPreferredConfig = Bitmap.Config.ARGB_8888
//            }
//            BitmapFactory.decodeResource(context.resources, drawableId, options)
            getBitmapFromResource(context, drawableId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Load image (from remote or local disk cache)
    suspend fun cacheRemoteImage(url: String, callback: (Bitmap?) -> Unit) = coroutineScope {
        // Check memory cache
        val cachedBitmap = bitmapCache.get(url)
        if (cachedBitmap != null) {
            callback(cachedBitmap)
            return@coroutineScope
        }

        // Check disk cache
        val diskCachedBitmap = loadBitmapFromDiskCache(url)
        if (diskCachedBitmap != null) {
            // Add to memory cache
            bitmapCache.put(url, diskCachedBitmap)
            callback(diskCachedBitmap)
            return@coroutineScope
        }

        // Download and cache
        launch(Dispatchers.IO) {
            val bitmap = downloadAndCacheImage(url)
            withContext(Dispatchers.Main) {
                callback(bitmap)
            }
        }
    }

    // Download image from remote and cache it
    private suspend fun downloadAndCacheImage(url: String): Bitmap? = coroutineScope {
        return@coroutineScope try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream
            val bitmap = decodeAndResizeBitmap(inputStream)
            bitmap?.let {
                bitmapCache.put(url, it)
                saveBitmapToDiskCache(it, url)
            }
            connection.disconnect()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Decode and resize bitmap
    private suspend fun decodeAndResizeBitmap(inputStream: InputStream): Bitmap? = coroutineScope {
        return@coroutineScope try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            // Read dimensions
            inputStream.mark(inputStream.available())
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.reset()

            // Calculate inSampleSize (resize factor)
            options.inSampleSize = calculateInSampleSize(options, 512, 512) // Target size: 512x512
            options.inJustDecodeBounds = false

            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Calculate resize factor
    private suspend fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int =
        coroutineScope {
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return@coroutineScope inSampleSize
        }

    // Save bitmap to disk cache
    private suspend fun saveBitmapToDiskCache(bitmap: Bitmap, url: String) = coroutineScope {
        try {
            val file = File(diskCacheDir, url.hashCode().toString())
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Load bitmap from disk cache
    private suspend fun loadBitmapFromDiskCache(url: String): Bitmap? = coroutineScope {
        return@coroutineScope try {
            val file = File(diskCacheDir, url.hashCode().toString())
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Retrieve bitmap from memory cache
    suspend fun getBitmap(key: String): Bitmap? = coroutineScope {
        timber("Materials", "bitmapCache=${bitmapCache.snapshot()}")
        return@coroutineScope bitmapCache.get(key)
    }

}
