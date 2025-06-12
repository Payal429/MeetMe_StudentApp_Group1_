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
        enableEdgeToEdge()
//        setContentView(R.layout.activity_support)

        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val backArrow = findViewById<ImageView>(R.id.backArrow)

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
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