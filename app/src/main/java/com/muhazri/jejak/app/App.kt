package com.muhazri.jejak.app

import android.app.Application
import com.muhazri.jejak.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        configureOsmdroid()
    }

    /**
     * Must run before the first MapView exists. The OSM tile policy requires a real User-Agent —
     * the library's default is blocked — and the cache lives in app storage so no storage
     * permission is needed on any API level.
     */
    private fun configureOsmdroid() {
        Configuration.getInstance().apply {
            userAgentValue = BuildConfig.APPLICATION_ID
            osmdroidBasePath = cacheDir.resolve("osmdroid")
            osmdroidTileCache = cacheDir.resolve("osmdroid/tiles")
        }
    }
}
