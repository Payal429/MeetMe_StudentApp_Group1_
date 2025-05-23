package com.group1.meetme

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

// Object to handle showing notifications.
object NotificationHelper {
    // Function to show a notification.
    fun showNotification(context: Context, title: String, message: String) {
        // Define the channel ID for the notification.
        val channelId = "reminder_channel"
        // Get the NotificationManager service.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8+ (API level 26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a new NotificationChannel with the specified ID, name, and importance level.
            val channel = NotificationChannel(
                channelId,
                "Appointment Reminders", // Channel name.
                NotificationManager.IMPORTANCE_HIGH // Importance level.
            )
            // Create the notification channel.
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification.
        val builder = NotificationCompat.Builder(context, channelId)
            // Set the small icon for the notification.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // Set the title of the notification.
            .setContentTitle(title)
            // Set the content text of the notification.
            .setContentText(message)
            // Set the priority of the notification.
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Notify the notification manager to show the notification.
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
