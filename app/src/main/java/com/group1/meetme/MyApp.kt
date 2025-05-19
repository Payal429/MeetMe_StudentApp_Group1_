package com.group1.meetme

import android.app.Application
import me.pushy.sdk.Pushy

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Pushy listener in the background
        Pushy.listen(this)
    }
}