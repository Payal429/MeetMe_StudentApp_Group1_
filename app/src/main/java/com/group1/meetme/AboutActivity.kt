package com.group1.meetme

// Import necessary packages
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group1.meetme.databinding.ActivityAboutBinding
import java.util.Locale

class AboutActivity : AppCompatActivity() {

    // View binding for the AboutActivity layout
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables the app to draw behind the system bars (status bar and navigation bar)
        enableEdgeToEdge()

        // Inflate the layout using view binding
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjusts padding of the root view to avoid overlapping system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // When back arrow is clicked, close this activity and return to the previous screen
        binding.backArrow.setOnClickListener {
            // Go back to SettingsFragment
            finish()
        }
        // Load saved language preference
        loadLanguage()
    }

    // Load saved language from SharedPreferences and apply it
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Change the locale of the app to the selected language
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}