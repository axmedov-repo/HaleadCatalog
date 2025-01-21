package com.halead.catalog.app

import android.app.Application
import com.halead.catalog.BuildConfig
import com.halead.catalog.data.DataProvider
import com.halead.catalog.data.cache.BitmapCacheManager
import com.halead.catalog.utils.timber
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var bitmapCacheManager: BitmapCacheManager

    @Inject
    lateinit var dataProvider: DataProvider

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            bitmapCacheManager.cacheLocalImages(dataProvider.materialsResIds)
        }
        // Initialize OpenCV
         if (!OpenCVLoader.initDebug()) {
            timber("OpenCV", "OpenCV initialization failed")
         } else {
             timber("OpenCV", "OpenCV initialized successfully")
         }

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
