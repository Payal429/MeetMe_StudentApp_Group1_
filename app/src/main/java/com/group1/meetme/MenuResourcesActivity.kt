package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MenuResourcesActivity : AppCompatActivity() {

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the user interface layout for this Activity
        setContentView(R.layout.activity_menu_resources)

        // --- UI COMPONENT REFERENCES ---

        // Reference to the "Upload Resources" card layout
        val uploadLayout = findViewById<LinearLayout>(R.id.uploadResourcesLayout)

        // Reference to the "Download Resources" card layout
        val downloadLayout = findViewById<LinearLayout>(R.id.downloadResourcesLayout)

        // Reference to the back arrow ImageButton (used to return to dashboard)
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        // --- CLICK LISTENERS ---

        // Navigate to UploadResourcesActivity when the upload card is tapped
        uploadLayout.setOnClickListener {
            val intent = Intent(this, UploadResourcesActivity::class.java)
            startActivity(intent)
        }

        // Navigate to DownloadResourcesActivity when the download card is tapped
        downloadLayout.setOnClickListener {
            val intent = Intent(this, DownloadResourcesActivity::class.java)
            startActivity(intent)
        }

        // Navigate back to StudentDashboardActivity when the back arrow is tapped
        backArrow.setOnClickListener {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        // Load and apply the previously saved language preference
        loadLanguage()
    }

    // Loads the user's preferred language setting from SharedPreferences
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // Retrieve saved language code, default to English ("en") if none found
        val savedLanguage = sharedPref.getString("language", "en")

        // Apply the language
        setLocale(savedLanguage ?: "en")
    }

    // Sets the app's locale (language) to the specified language code
    private fun setLocale(languageCode: String) {
        // Create a new Locale object with the specified language
        val locale = Locale(languageCode)

        // Set the locale as the default for the app
        Locale.setDefault(locale)

        // Update the current configuration with the new locale
        val config = resources.configuration
        config.setLocale(locale)

        // Apply the updated configuration to the app's resources
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
