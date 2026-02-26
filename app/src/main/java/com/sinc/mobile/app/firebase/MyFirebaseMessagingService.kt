package com.sinc.mobile.app.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sinc.mobile.MainActivity
import com.sinc.mobile.R
import com.sinc.mobile.data.session.SessionManager
import com.sinc.mobile.domain.use_case.auth.SendFcmTokenUseCase
import com.sinc.mobile.domain.use_case.notification.SaveNotificationUseCase
import com.sinc.mobile.domain.use_case.weather.SyncWeatherAlertsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var sendFcmTokenUseCase: SendFcmTokenUseCase
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var saveNotificationUseCase: SaveNotificationUseCase
    @Inject
    lateinit var syncWeatherAlertsUseCase: SyncWeatherAlertsUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Handle DATA payload first
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            handleDataMessage(remoteMessage.data)
        }
        // Fallback to NOTIFICATION payload if no data payload or no specific data type
        else {
            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
                val title = it.title ?: getString(R.string.app_name)
                val body = it.body ?: "Nueva notificación"

                serviceScope.launch {
                    saveNotificationUseCase(title, body, null, null)
                }
                sendNotification(title, body, null, System.currentTimeMillis().toInt())
            }
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        val screen = data["screen"] // e.g., "HomeScreen", "TicketConversationScreen"
        val notificationTitle = data["title"] ?: getString(R.string.app_name)
        val notificationBody = data["body"] ?: "Nueva notificación"

        var pendingIntent: PendingIntent? = null
        var notificationId: Int = System.currentTimeMillis().toInt() // Default unique ID

        when (type) {
            "ticket_response" -> {
                val ticketId = data["ticket_id"]?.toLongOrNull()
                if (ticketId != null) {
                    pendingIntent = createTicketConversationPendingIntent(ticketId)
                    notificationId = ticketId.toInt() // Use ticket ID for consistent notification updates
                }
            }
            "unidad_productiva_reminder" -> { // Assuming this type for the "register UP" reminder
                // This notification points to the home screen (or a specific UP registration flow)
                // For now, it will open the app to the home screen (generic intent)
                pendingIntent = createGenericAppPendingIntent()
                // A specific notification ID could be used for reminders if we want to update it
                notificationId = UNIDAD_PRODUCTIVA_REMINDER_ID
            }
            "weather_alert" -> {
                pendingIntent = createGenericAppPendingIntent()
                serviceScope.launch {
                    syncWeatherAlertsUseCase()
                }
            }
            else -> {
                // Handle generic data messages based on 'screen' hint or default to app launch
                when (screen) {
                    "HomeScreen" -> {
                        pendingIntent = createGenericAppPendingIntent()
                    }
                    // Add more cases for other screens if needed
                    else -> {
                        pendingIntent = createGenericAppPendingIntent()
                    }
                }
            }
        }

        serviceScope.launch {
            saveNotificationUseCase(notificationTitle, notificationBody, type, data)
        }
        sendNotification(notificationTitle, notificationBody, pendingIntent, notificationId)
    }

    private fun createTicketConversationPendingIntent(ticketId: Long): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_OPEN_TICKET_CONVERSATION
            putExtra(EXTRA_TICKET_ID, ticketId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            ticketId.toInt(), // Use ticketId as a unique request code
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE is required for Android 12+
        )
    }

    private fun createGenericAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            this,
            0, // Generic request code
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        serviceScope.launch {
            if (sessionManager.getAuthToken() != null) {
                when(val result = sendFcmTokenUseCase(token)) {
                    is com.sinc.mobile.domain.util.Result.Success -> {
                        Log.d(TAG, "Refreshed FCM Token sent successfully.")
                    }
                    is com.sinc.mobile.domain.util.Result.Failure -> {
                        Log.e(TAG, "Failed to send refreshed FCM Token: ${result.error.message}")
                    }
                }
            } else {
                Log.w(TAG, "User not logged in. Refreshed FCM Token will not be sent.")
            }
        }
    }

    private fun sendNotification(messageTitle: String, messageBody: String, pendingIntent: PendingIntent?, notificationId: Int) {
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use an app icon here
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        pendingIntent?.let {
            notificationBuilder.setContentIntent(it)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones Generales",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        const val ACTION_OPEN_TICKET_CONVERSATION = "com.sinc.mobile.ACTION_OPEN_TICKET_CONVERSATION"
        const val EXTRA_TICKET_ID = "ticketId"
        const val UNIDAD_PRODUCTIVA_REMINDER_ID = 1001 // Unique ID for this type of reminder
    }
}
