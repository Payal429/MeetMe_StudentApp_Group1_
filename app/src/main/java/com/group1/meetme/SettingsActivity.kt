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
        // Load the layout defined in activity_settings.xml
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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

    // Loads saved language preference from shared preferences and applies it
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Changes the app's locale/language at runtime
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        // Update the app's configuration to use the new locale
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Retrieves the stored user type (either "student" or "lecturer") from shared preferences
    private fun getUserType(): String {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return prefs.getString("userType", "student") ?: "student"
    }

    // Navigates back to the appropriate dashboard based on user type
    private fun navigateToDashboard() {
        // Close current settings activity
        finish()
        // Get user type from preferences
        val userType = getUserType()
        // Debug log to check userType
        Log.d(userType, "userType + $userType")

        // Redirect to the correct dashboard
        if (userType == "student"){
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, LecturerDashboardActivity::class.java))
        }
    }
}
