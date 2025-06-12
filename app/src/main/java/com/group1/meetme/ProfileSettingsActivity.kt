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

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var idText: TextView
    private lateinit var fullnameText: TextView
    private lateinit var emailText: TextView
    private lateinit var userTypeText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val backArrow = findViewById<ImageView>(R.id.backArrow)

        backArrow.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Bind views
        idText = findViewById(R.id.IDText)
        fullnameText = findViewById(R.id.fullnameText)
        emailText = findViewById(R.id.emailAddressText)
        userTypeText = findViewById(R.id.userTypeText)

        // Get the user's ID number from SharedPreferences.
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)
        val typeOfUser = sharedPreferences.getString("USER_ROLE", null)

        Log.d("userType", typeOfUser ?: "null")
        Log.d("userID", idNum?: "null")

        if (idNum!!.isNotEmpty() && typeOfUser!!.isNotEmpty()) {
            loadUserProfile(idNum, typeOfUser)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile(idNum: String, typeOfUser: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(typeOfUser).child(idNum)

        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString()
                val surname = snapshot.child("surname").value.toString()
                val email = snapshot.child("email").value.toString()

                // Set data to TextViews
                idText.text = idNum
                fullnameText.text = "$name $surname"
                emailText.text = email
                userTypeText.text = typeOfUser
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

}
