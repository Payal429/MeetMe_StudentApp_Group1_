
package com.group1.meetme

import android.app.Application
import com.cloudinary.android.MediaManager

class CloudinaryConnect : Application() {
    override fun onCreate() {
        super.onCreate()

        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "dneya4bco"
        config["api_key"] = "685364927312453"
        config["api_secret"] = "V3h2ETBDyTVfk2HxvCdkoRMuHvs"

        MediaManager.init(this, config)
    }
}