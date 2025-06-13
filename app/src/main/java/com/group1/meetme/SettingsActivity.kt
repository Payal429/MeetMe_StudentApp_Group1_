package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Handle back arrow click
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            navigateToDashboard()
        }

        // Navigate to About
        findViewById<LinearLayout>(R.id.aboutLayout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // Navigate to Profile Settings
        findViewById<LinearLayout>(R.id.profileLayout).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        // Navigate to Support
        findViewById<LinearLayout>(R.id.supportLayout).setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        // Navigate to App Settings
        findViewById<LinearLayout>(R.id.appSettingsLayout).setOnClickListener {
            startActivity(Intent(this, AppSettingsActivity::class.java))
        }

        // Load saved language preference
        loadLanguage()

    }

    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun getUserType(): String {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return prefs.getString("userType", "student") ?: "student"
    }

    private fun navigateToDashboard() {
        finish()
        val userType = getUserType()
        Log.d(userType, "userType + $userType")
        if (userType == "student"){
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LecturerDashboardActivity::class.java))
        }
//        val intent = Intent(this, DashboardActivity::class.java)

//        startActivity(getIntent())
    }


}
