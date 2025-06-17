package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import java.util.Locale

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to About Activity
        view.findViewById<LinearLayout>(R.id.aboutLayout).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        // Navigate to Profile Settings Activity
        view.findViewById<LinearLayout>(R.id.profileLayout).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSettingsActivity::class.java))
        }

        // Navigate to Support Activity
        view.findViewById<LinearLayout>(R.id.supportLayout).setOnClickListener {
            startActivity(Intent(requireContext(), SupportActivity::class.java))
        }

        // Navigate to App Settings Activity
        view.findViewById<LinearLayout>(R.id.appSettingsLayout).setOnClickListener {
            startActivity(Intent(requireContext(), AppSettingsActivity::class.java))
        }

        // Load saved language preference
        loadLanguage()
    }

    // Load the saved language from SharedPreferences and apply locale
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default English
        setLocale(savedLanguage ?: "en")
    }

    // Set and apply the selected locale to the app
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Retrieves the stored user type (either "student" or "lecturer") from shared preferences
    private fun getUserType(): String {
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return prefs.getString("userType", "student") ?: "student"
    }

    // Navigates back to the appropriate dashboard based on user type
    private fun navigateToDashboard() {
        val userType = getUserType()
        Log.d("SettingsFragment", "userType = $userType")
        if (userType == "student") {
            startActivity(Intent(requireContext(), StudentMainActivity::class.java))
        } else {
            startActivity(Intent(requireContext(), LecturerMainActivity::class.java))
        }
        // If you want to remove this fragment after navigating:
        requireActivity().finish()
    }
}
