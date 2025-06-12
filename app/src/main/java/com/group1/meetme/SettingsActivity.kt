package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Handle back arrow click
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            navigateToDashboard()
        }

//        // Navigate to Account Settings
//        findViewById<LinearLayout>(R.id.aboutLayout).setOnClickListener {
//            startActivity(Intent(this, AboutActivity::class.java))
//        }

        // Navigate to Profile Settings
        findViewById<LinearLayout>(R.id.profileLayout).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

//        // Navigate to App Settings
//        findViewById<LinearLayout>(R.id.supportLayout).setOnClickListener {
//            startActivity(Intent(this, SupportActivity::class.java))
//        }
    }

    private fun getUserType(): String {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        return prefs.getString("userType", "student") ?: "student"
    }

    private fun navigateToDashboard() {
//        val intent = if (getUserType() == "student") {
//            Intent(this, StudentDashboardActivity::class.java)
//        } else {
//            Intent(this, LecturerDashboardActivity::class.java)
//        }
//        startActivity(intent)
        finish()
    }


}
