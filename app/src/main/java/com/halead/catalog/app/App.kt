package com.halead.catalog.app

import android.app.Application
import com.halead.catalog.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize OpenCV
        /* if (!OpenCVLoader.initDebug()) {
            timber("OpenCV", "OpenCV initialization failed")
         } else {
             timber("OpenCV", "OpenCV initialized successfully")
         }*/

        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
