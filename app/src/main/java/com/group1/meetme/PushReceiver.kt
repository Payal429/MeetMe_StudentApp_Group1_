package com.group1.meetme

import me.pushy.sdk.Pushy
import android.content.Intent
import android.graphics.Color
import android.content.Context
import android.app.PendingIntent
import android.media.RingtoneManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import androidx.core.app.NotificationCompat
import android.content.Context.NOTIFICATION_SERVICE

// BroadcastReceiver to handle reminders.
class PushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Attempt to extract the "title" property from the data payload, or fallback to app shortcut label
        val notificationTitle =
            if (intent.getStringExtra("title") != null) intent.getStringExtra("title") else context.packageManager.getApplicationLabel(
                context.applicationInfo
            ).toString()

        // Attempt to extract the "message" property from the data payload: {"message":"Hello World!"}
        var notificationText =
            if (intent.getStringExtra("message") != null) intent.getStringExtra("message") else "Test notification"

        // Prepare a notification with vibration, sound and lights
        val builder = NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.app_logo_icon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        // Automatically configure a Notification Channel for devices running Android O+
        Pushy.setNotificationChannel(builder, context)

        // Get an instance of the NotificationManager service
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification and display it
        // Use a random notification ID so multiple
        // notifications don't overwrite each other
        notificationManager.notify((Math.random() * 100000).toInt(), builder.build())

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, builder.build())
    }
}