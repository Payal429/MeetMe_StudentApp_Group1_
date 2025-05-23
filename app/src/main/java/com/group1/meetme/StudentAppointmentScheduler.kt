package com.group1.meetme

import android.app.Application
import com.cloudinary.android.MediaManager

// Custom Application class to initialize Cloudinary MediaManager for student appointment scheduling.
class StudentAppointmentScheduler : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary configuration.
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "FirstKey"
        config["api_key"] = "685364927312453"
        config["api_secret"] = "V3h2ETBDyTVfk2HxvCdkoRMuHvs"
        // Initialize MediaManager with the provided configuration.
        MediaManager.init(this, config)
    }
}