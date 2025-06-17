package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MenuResourcesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for this activity (activity_menu_resources.xml)
        setContentView(R.layout.activity_menu_resources)

        // Reference to the Upload Resources card
        val uploadLayout = findViewById<LinearLayout>(R.id.uploadResourcesLayout)

        // Reference to the Download Resources card
        val downloadLayout = findViewById<LinearLayout>(R.id.downloadResourcesLayout)

        // Open UploadResourcesActivity when Upload card is tapped
        uploadLayout.setOnClickListener {
            val intent = Intent(this, UploadResourcesActivity::class.java)
            startActivity(intent)
        }

        // Open DownloadResourcesActivity when Download card is tapped
        downloadLayout.setOnClickListener {
            val intent = Intent(this, DownloadResourcesActivity::class.java)
            startActivity(intent)
        }
        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener() {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }
        // Load saved language preference
        loadLanguage()
    }

    // Loads the language setting saved in SharedPreferences
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Applies the selected locale (language) to the app
    private fun setLocale(languageCode: String) {
        // Create Locale object from language code
        val locale = Locale(languageCode)
        // Set as the app’s default locale
        Locale.setDefault(locale)

        // Get current configuration and update its locale
        val config = resources.configuration
        config.setLocale(locale)

        // Update app's resources with the new configuration
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}