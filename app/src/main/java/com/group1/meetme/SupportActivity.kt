package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group1.meetme.databinding.ActivitySupportBinding
import java.util.Locale

class SupportActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables edge-to-edge display for immersive UI experience
        enableEdgeToEdge()

        // Inflate the layout and set the root view as the content view
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set padding for the main view to handle system window insets (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to the view to avoid overlap with system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listener on the back arrow icon to finish the activity and go back
        binding.backArrow.setOnClickListener {
            finish()
        }

        // Load saved language preference
        loadLanguage()

    }

    // Loads the saved language code from SharedPreferences and applies it
    private fun loadLanguage() {
        // Access shared preferences named "AppSettings" in private mode
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        // Get saved language code, default to "en" (English) if not found
        val savedLanguage = sharedPref.getString("language", "en")
        // Set app locale using the retrieved language code
        setLocale(savedLanguage ?: "en")
    }

    // Changes the app's locale (language) to the given language code
    private fun setLocale(languageCode: String) {
        // Create Locale object from language code
        val locale = Locale(languageCode)
        // Set default locale globally
        Locale.setDefault(locale)
        // Get current resource configuration
        val config = resources.configuration
        // Set new locale in configuration
        config.setLocale(locale)
        // Apply new configuration to resources
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}