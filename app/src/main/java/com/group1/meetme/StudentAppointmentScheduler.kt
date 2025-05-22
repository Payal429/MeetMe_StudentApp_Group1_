package com.group1.meetme

import android.app.Application
import com.cloudinary.android.MediaManager

class StudentAppointmentScheduler : Application() {
    override fun onCreate() {
        super.onCreate()
        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "FirstKey"
        config["api_key"] = "685364927312453"
        config["api_secret"] = "V3h2ETBDyTVfk2HxvCdkoRMuHvs"
        MediaManager.init(this, config)
    }
}