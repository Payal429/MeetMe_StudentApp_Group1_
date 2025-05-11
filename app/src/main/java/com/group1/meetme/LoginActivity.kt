package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the login button by its ID
        val loginButton: Button = findViewById(R.id.loginButton)
        val emailEditText: TextInputEditText = findViewById(R.id.email)
        val passwordEditText: TextInputEditText = findViewById(R.id.password)

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener {
            // Get the email and password entered by the user
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if the email and password fields are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Define the default admin credentials
            val adminEmail = "admin@example.com"
            val adminPassword = "admin123"

            // Check if the entered credentials match the admin credentials
            if (email == adminEmail && password == adminPassword) {
                // Navigate to AdminDashboardActivity
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
            }
//            else {
//                // For simplicity, let's assume students have "student" in their email and lecturers have "lecturer"
//                if (email.contains("student")) {
//                    // Navigate to StudentDashboardActivity
//                    val intent = Intent(this, student_dashboard::class.java)
//                    startActivity(intent)
//                } else if (email.contains("lecturer")) {
//                    // Navigate to LecturerDashboardActivity
//                    val intent = Intent(this, student_dashboard::class.java)
//                    startActivity(intent)
//                } else {
//                    // Show an error message if the credentials are invalid
//                    Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }
}