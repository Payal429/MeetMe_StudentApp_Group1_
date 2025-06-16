package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.group1.meetme.databinding.ActivityAppSettingsBinding
import java.util.Locale

class AppSettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
//    private lateinit var radioEnglish: RadioButton
//    private lateinit var radioAfrikaans: RadioButton

    // binding for the activity
    private lateinit var binding : ActivityAppSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_app_settings) // Change to your layout's name if different

        binding = ActivityAppSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val backArrow = binding.backArrow

//        val backArrow = findViewById<ImageView>(R.id.backArrow)
//        binding.backArrow.setOnClickListener {
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//            finish() // Optional: finishes current activity so it's removed from back stack
//        }
        binding.backArrow.setOnClickListener {
            finish() // Go back to SettingsFragment
        }

//        val saveSettingsBtn = findViewById<Button>(R.id.saveSettingsBtn)
//        binding.radioEnglish = findViewById<RadioButton>(R.id.radioEnglish)
//        binding.radioAfrikaans = findViewById<RadioButton>(R.id.radioAfrikaans)


        binding.saveSettingsBtn.setOnClickListener {
            // Handle save button click
            finish()
            // Get selected language
            val selectedLanguage = if (binding.radioEnglish.isChecked) "en" else "af"

            // Save the selected language in SharedPreferences
            saveLanguagePreference(selectedLanguage)

            // Apply the language change and restart the app to reflect changes
            setLocale(selectedLanguage)

//            saveSettings()
//            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()

            Toast.makeText(this, "Settings Saved Successfully", Toast.LENGTH_SHORT).show()
        }

        val reminderSpinner = findViewById<Spinner>(R.id.reminderSpinner)
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Load saved preference
        val savedReminderTime = sharedPrefs.getInt("reminderHours", 24)
        val spinnerIndex = when (savedReminderTime) {
            24 -> 0
            12 -> 1
            6 -> 2
            1 -> 3
            else -> 0
        }
        reminderSpinner.setSelection(spinnerIndex)

        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val hours = when (position) {
                    0 -> 24
                    1 -> 12
                    2 -> 6
                    3 -> 1
                    else -> 24
                }
                sharedPrefs.edit().putInt("reminderHours", hours).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }



        // Load saved language preference
        loadSavedLanguage()

        sharedPreferences = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)



    }

    private fun saveLanguagePreference(language: String) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("language", language)
        editor.apply()
    }

    private fun loadSavedLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English

        // Set the appropriate radio button based on the saved preference
        if (savedLanguage == "af") {
            binding.radioAfrikaans.isChecked = true
        } else {
            binding.radioEnglish.isChecked = true
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart the activity to apply the language change across the app
        val intent = Intent(this, SettingsActivity::class.java)  // You may change this to your launch activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
