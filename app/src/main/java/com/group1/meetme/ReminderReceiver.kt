package com.group1.meetme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// BroadcastReceiver to handle reminders.
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve the title, message, and lecturer token from the intent.
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")
        val lecturerToken = intent.getStringExtra("lecturerToken")

        // Show local notification to student
        NotificationHelper.showNotification(context, title ?: "", message ?: "")

        // Send remote Pushy notification to lecturer
        lecturerToken?.let {
            sendPushyNotification(
                it,
                title ?: "Reminder",
                message ?: "Upcoming appointment reminder."
            )
        }
    }

    // Function to send a Pushy notification.
    private fun sendPushyNotification(deviceToken: String, title: String, message: String) {
        // Create an OkHttpClient instance.
        val client = OkHttpClient()
        // Create a JSON object for the notification payload.
        val json = JSONObject().apply {
            // Set the recipient's device token.
            put("to", deviceToken)
            // Set the notification title.
            put("data", JSONObject().apply {
                // Set the notification title.
                put("title", title)
                // Set the notification message.
                put("message", message)
            })
        }

        // Convert the JSON object to a request body.
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        // Build the request.
        val request = Request.Builder()
            // Pushy API endpoint.
            .url("https://api.pushy.me/push?api_key=e7a666413409280a1c85d6471083b3b933c7e411c6f5ab97251a0e216d8b7696")
            // Set the request
            .post(requestBody)
            .build()

        // Enqueue the request.
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Log an error if the request fails.
                Log.e("Pushy", "Reminder failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                // Log the response if the request is successful.
                Log.d("Pushy", "Reminder sent: ${response.body?.string()}")
            }
        })
    }
}
