package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group1.meetme.databinding.ActivityAdminDashboardBinding
import java.util.Locale

// This activity represents the Admin Dashboard.
// It provides buttons for adding new students, adding new lecturers, managing users, and accessing reports and analytics.
// The activity also handles the back button press to prompt the admin for confirmation before logging out.
class AdminDashboardActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the layout using view binding
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Adjust padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set an OnClickListener for the "Add New Student" button
        binding.addNewStudentButton.setOnClickListener {
            val student = "Student"

            // Navigate to AddNewStudentActivity
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra("userType", student);
            startActivity(intent)
        }

        // Set an OnClickListener for the "Add New Lecturer" button
        binding.addNewLecturerButton.setOnClickListener {
            // Navigate to AddNewLecturerActivity
            val lecturer = "Lecturer"
            val intent = Intent(this, AddUserActivity::class.java)
            intent.putExtra("userType", lecturer);
            startActivity(intent)
        }

        // Set an OnClickListener for the "Manage Users" button
        binding.manageUsersButton.setOnClickListener {
            // Navigate to ManageUsersActivity
            val intent = Intent(this, ManageUsersActivity::class.java)
            startActivity(intent)
        }

        // Set an OnClickListener for the "Reports and Analytics" button
        binding.reportsAnalyticsButton.setOnClickListener {
            // Navigate to ReportsAnalyticsActivity
            val intent = Intent(this, AdminAnalyticsActivity::class.java)
            startActivity(intent)
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

    // Override the back button press to prompt the admin for confirmation before logging out.
    override fun onBackPressed() {
        // Create an AlertDialog to confirm the logout action.
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to Logout?")

        // If "Yes" is selected, clear session data and close app
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, which ->
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("ID_NUM", "")
            editor.apply()

            super.onBackPressed()
            // Finish all activities and exit the app gracefully
            finishAffinity()
            dialog.dismiss()
        }

        // If "No" is selected, dismiss the dialog
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which ->
            dialog.dismiss()
        }
        // Show confirmation dialog
        alertDialog.show()
    }
}