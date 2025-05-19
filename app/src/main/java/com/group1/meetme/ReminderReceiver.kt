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

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")
        val lecturerToken = intent.getStringExtra("lecturerToken")

        // Show local notification to student
        NotificationHelper.showNotification(context, title ?: "", message ?: "")

        // Send remote Pushy notification to lecturer
        lecturerToken?.let {
            sendPushyNotification(it, title ?: "Reminder", message ?: "Upcoming appointment reminder.")
        }
    }

    private fun sendPushyNotification(deviceToken: String, title: String, message: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("to", deviceToken)
            put("data", JSONObject().apply {
                put("title", title)
                put("message", message)
            })
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.pushy.me/push?api_key=e7a666413409280a1c85d6471083b3b933c7e411c6f5ab97251a0e216d8b7696")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Pushy", "Reminder failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Pushy", "Reminder sent: ${response.body?.string()}")
            }
        })
    }
}
