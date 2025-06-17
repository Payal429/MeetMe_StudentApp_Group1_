package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.group1.meetme.databinding.ActivityProfileSettingsBinding
import java.util.Locale

class ProfileSettingsActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding: ActivityProfileSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding and set it as the content view
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set listener on the back arrow icon to return to the previous screen (SettingsFragment)
        binding.backArrow.setOnClickListener {
            // Go back to SettingsFragment
            finish()
        }

        // Get the user's ID number from SharedPreferences.
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)
        val typeOfUser = sharedPreferences.getString("USER_ROLE", null)

        // Debug logging to confirm retrieved values
        Log.d("userType", typeOfUser ?: "null")
        Log.d("userID", idNum ?: "null")

        // Only proceed if both ID number and user type are not null or empty
        if (idNum!!.isNotEmpty() && typeOfUser!!.isNotEmpty()) {
            loadUserProfile(idNum, typeOfUser)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
        // Load saved language preference
        loadLanguage()
    }

    // Load the saved language from SharedPreferences and apply locale
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
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

    private fun loadUserProfile(idNum: String, typeOfUser: String) {
        // Reference to the user's node in Firebase Realtime Database
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(typeOfUser).child(idNum)

        // Read data once using addOnSuccessListener
        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Extract fields from the snapshot
                val name = snapshot.child("name").value.toString()
                val surname = snapshot.child("surname").value.toString()
                val email = snapshot.child("email").value.toString()

                // Set data to TextViews
                binding.IDText.text = idNum
                binding.fullnameText.text = "$name $surname"
                binding.emailAddressText.text = email
                binding.userTypeText.text = typeOfUser
            } else {
                // Handle case where user data is not found in the database
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Handle error if the database operation fails
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }
}
