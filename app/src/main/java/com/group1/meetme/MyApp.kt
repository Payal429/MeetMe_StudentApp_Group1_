package com.group1.meetme

import android.app.Application
import me.pushy.sdk.Pushy

// Custom Application class to initialize Pushy SDK.
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Pushy listener in the background to handle incoming push notifications.
        Pushy.listen(this)
    }
}