package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Locale

// 2 May 2025
// Payal and Nyanda
// Welcome Page

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply animations to views
        findViewById<LinearLayout>(R.id.main).startAnimation(fadeIn)
        findViewById<ImageView>(R.id.logo).startAnimation(fadeIn)
        findViewById<TextView>(R.id.textViewSubtitle).startAnimation(slideUp)
        findViewById<ImageView>(R.id.elephant).startAnimation(slideUp)
        findViewById<Button>(R.id.buttonGetStarted).startAnimation(slideUp)

        // Find the button by its ID
        val getStartedButton: Button = findViewById(R.id.buttonGetStarted)

        // Set an OnClickListener for the button
        getStartedButton.setOnClickListener {
            // Create an Intent to navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            // Start the LoginActivity
            startActivity(intent)
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
}