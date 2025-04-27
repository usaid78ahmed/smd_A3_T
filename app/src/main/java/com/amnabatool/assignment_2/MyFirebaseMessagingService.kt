
package com.amnabatool.assignment_2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFCMService"
        private const val CHANNEL_ID = "default_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
        // Send token to your backend
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.data.let { data ->
            Log.d(TAG, "Data payload: $data")
            when (data["type"]) {
                "screenshot_alert" -> handleScreenshotAlert(data)
                else -> handleRegularMessage(remoteMessage)
            }
        }
    }

    private fun handleRegularMessage(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            sendNotification(
                notification.title ?: "New Message",
                notification.body ?: "You have a new message",
                true
            )
        }
    }

    private fun handleScreenshotAlert(data: Map<String, String>) {
        sendNotification(
            data["title"] ?: "Privacy Alert",
            data["body"] ?: "A screenshot was detected!",
            true
        )
    }

    private fun sendNotification(title: String, message: String, isHighPriority: Boolean) {
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setLights(Color.BLUE, 1000, 1000)
            .setPriority(
                if (isHighPriority) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        try {
            if (hasNotificationPermission()) {
                NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, notificationBuilder.build())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission error", e)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for chat notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
