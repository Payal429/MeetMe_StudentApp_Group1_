package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val apiService: ApiService = ApiClient.create(ApiService::class.java)

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
            } else {
                login(email, password)
            }
        }
    }

    private fun login(idNum: String, password: String) {
        val loginRequest = VerifyOtpRequest(idNum, password)
        val userId = GetUser(idNum)

        apiService.loginWithOtp(loginRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.message == "OTP verified. Please set your password.") {
                    val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("ID_NUM", idNum)
                    editor.apply()
                    // OTP login successful, redirect to ChangePasswordActivity
                    startActivity(Intent(this@LoginActivity, ChangePasswordActivity::class.java))
                } else {
                    // Attempt subsequent login
                    val changePasswordRequest = ChangePassword(idNum, password)
                    apiService.login(changePasswordRequest).enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                // Successful login, get user type and redirect accordingly
                                apiService.getUserType(userId).enqueue(object : Callback<UserTypeResponse> {
                                    override fun onResponse(call: Call<UserTypeResponse>, response: Response<UserTypeResponse>) {
                                        if (response.isSuccessful) {
                                            val userType = response.body()?.typeOfUser
                                            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                                            val editor = sharedPreferences.edit()
                                            editor.putString("ID_NUM", idNum)
                                            editor.apply()

                                            Log.d("userType", userType ?: "null")
                                            Log.d("userID", userId.idNum)

                                            when (userType) {
                                                "Student" -> {finish()
                                                    startActivity(Intent(this@LoginActivity, StudentDashboardActivity::class.java))}
                                                "Lecturer" -> {finish()
                                                    startActivity(Intent(this@LoginActivity, LecturerDashboardActivity::class.java))}
                                                else -> Toast.makeText(this@LoginActivity, "Unknown user type.", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(this@LoginActivity, "Failed to get user type.", Toast.LENGTH_SHORT).show()
                                            Log.e("API_ERROR", "Error: ${response.code()}, ${response.errorBody()?.string()}")
                                        }
                                    }

                                    override fun onFailure(call: Call<UserTypeResponse>, t: Throwable) {
                                        Toast.makeText(this@LoginActivity, "Failed to get user type.", Toast.LENGTH_SHORT).show()
                                        Log.e("API_FAILURE", "Failed to make request: ${t.message}")
                                    }
                                })
                            } else {
                                Toast.makeText(this@LoginActivity, "Invalid credentials.", Toast.LENGTH_SHORT).show()
                                Log.e("API_ERROR", "Error: ${response.code()}, ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "Login failed.", Toast.LENGTH_SHORT).show()
                            Log.e("API_FAILURE", "Failed to make request: ${t.message}")
                        }
                    })
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_FAILURE", "Failed to make request: ${t.message}")
            }
        })
    }
}