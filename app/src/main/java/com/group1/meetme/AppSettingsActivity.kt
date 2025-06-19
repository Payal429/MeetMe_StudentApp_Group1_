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

    // Declare a reference to SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    // binding for the activity
    private lateinit var binding: ActivityAppSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityAppSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle back arrow click to return to previous screen (likely SettingsFragment)
        binding.backArrow.setOnClickListener {
            // Go back to SettingsFragment
            finish()
        }

        binding.saveSettingsBtn.setOnClickListener {
            // Get selected language
            val selectedLanguage = if (binding.radioEnglish.isChecked) "en" else "af"

            // Save the selected language in SharedPreferences
            saveLanguagePreference(selectedLanguage)

            // Apply the language change and restart the app to reflect changes
            setLocale(selectedLanguage)

            // Inform the user
            Toast.makeText(this, "Settings Saved Successfully", Toast.LENGTH_SHORT).show()

            // Tell the previous activity to restart itself
            val intent = Intent()
            intent.putExtra("LANGUAGE_CHANGED", true)
            setResult(RESULT_OK, intent)

            finish()
        }


        // Spinner to set reminder notification time
        val reminderSpinner = findViewById<Spinner>(R.id.reminderSpinner)
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Load previously saved reminder hour (default to 24)
        val savedReminderTime = sharedPrefs.getInt("reminderHours", 24)

        // Set spinner selection based on saved value
        val spinnerIndex = when (savedReminderTime) {
            24 -> 0
            12 -> 1
            6 -> 2
            1 -> 3
            else -> 0
        }
        reminderSpinner.setSelection(spinnerIndex)

        // Save new selection when user chooses a different reminder time
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
                // Save selected reminder time to SharedPreferences
                sharedPrefs.edit().putInt("reminderHours", hours).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // Load saved language preference
        loadSavedLanguage()

        // Initialize another SharedPreferences instance (not used further here)
        sharedPreferences = getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
    }

    //  Save the selected language to SharedPreferences
    private fun saveLanguagePreference(language: String) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("language", language)
        editor.apply()
    }

    // Load the saved language preference from SharedPreferences and apply it
    private fun loadSavedLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        // Default to English
        val savedLanguage = sharedPref.getString("language", "en")

        // Set the appropriate radio button based on the saved preference
        if (savedLanguage == "af") {
            binding.radioAfrikaans.isChecked = true
        } else {
            binding.radioEnglish.isChecked = true
        }
    }

    // Change the app's locale to the selected language code
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

    }
}
