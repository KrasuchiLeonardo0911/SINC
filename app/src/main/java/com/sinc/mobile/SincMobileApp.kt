package com.sinc.mobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import com.sinc.mobile.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import com.sinc.mobile.R // Import R for string resources

@HiltAndroidApp
class SincMobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // --- osmdroid configuration ---
        // This loads the osmdroid configuration from shared preferences
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        // This line sets the user agent, a requirement to prevent getting banned from the OSM servers.
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        // --- FCM Notification Channel Configuration ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.app_name) // Using app_name as channel name
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Default channel for SINC Mobile notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
