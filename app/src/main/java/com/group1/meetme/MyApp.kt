package com.group1.meetme

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import me.pushy.sdk.Pushy

// Custom Application class to initialize Pushy SDK.
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Pushy listener in the background to handle incoming push notifications.
        Pushy.listen(this)

        // Enable Firebase Realtime Database offline persistence
        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)
    }
}