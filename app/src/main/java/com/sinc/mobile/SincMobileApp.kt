package com.sinc.mobile

import android.app.Application
import androidx.preference.PreferenceManager
import com.sinc.mobile.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class SincMobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // --- osmdroid configuration ---
        // This loads the osmdroid configuration from shared preferences
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        // This line sets the user agent, a requirement to prevent getting banned from the OSM servers.
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }
}
