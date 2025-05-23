
package com.group1.meetme

import android.app.Application
import com.cloudinary.android.MediaManager

// Application class to initialize Cloudinary MediaManager.
class CloudinaryConnect : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create a configuration map for Cloudinary.
        val config: HashMap<String, String> = HashMap()
        // Set the Cloudinary cloud name.
        config["cloud_name"] = "dneya4bco"
        // Set the Cloudinary API key.
        config["api_key"] = "685364927312453"
        // Set the Cloudinary API secret.
        config["api_secret"] = "V3h2ETBDyTVfk2HxvCdkoRMuHvs"

        // Initialize the MediaManager with the provided configuration.
        MediaManager.init(this, config)
    }
}