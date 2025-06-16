package com.group1.meetme

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.group1.meetme.databinding.ActivityLoginBinding
import me.pushy.sdk.Pushy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

// Activity for user login.
class LoginActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivityLoginBinding

    // Initialize the ApiService using the ApiClient.
    private val apiService: ApiService = ApiClient.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable offline persistence for Firebase Database.
        Firebase.database.setPersistenceEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Request permission (Android 13+)
        requestNotificationPermission()

        // 2. Create notification channel (Android 8+)
        createNotificationChannel()


        // Find the login button by its ID
//        val loginButton: Button = findViewById(R.id.loginButton)
//        val emailEditText: TextInputEditText = findViewById(R.id.email)
//        val passwordEditText: TextInputEditText = findViewById(R.id.password)
//        val forgotPasswordButton = findViewById<TextView>(R.id.forgotPassword)

        // Set an OnClickListener for the login button
        binding.loginButton.setOnClickListener {
            // Get the email and password entered by the user
            val username = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // Check if the email and password fields are not empty
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Define the default admin credentials
            val adminEmail = "admin"
            val adminPassword = "admin123"

            // Check if the entered credentials match the admin credentials
            if (username == adminEmail && password == adminPassword) {
                // Navigate to AdminDashboardActivity
                val intent = Intent(this, AdminDashboardActivity::class.java)
                startActivity(intent)
            } else {
                login(username, password)
            }
        }

        // Set onclickListener for the forgot password button
        binding.forgotPassword.setOnClickListener {
            showAdminContactDialog()
        }
        // Load saved language preference
        loadLanguage()

    }

    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Function to show the message dialog for forgot password
    private fun showAdminContactDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password Assistance")
        builder.setMessage("Please contact Administration to reset your password.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Function to handle user login.
    private fun login(idNum: String, password: String) {
        // Create a login request object.
        val loginRequest = VerifyOtpRequest(idNum, password)
        val userId = GetUser(idNum)

        // Enqueue the API call to verify OTP.
        apiService.loginWithOtp(loginRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful && response.body()?.message == "OTP verified. Please set your password.") {
                    val sharedPreferences =
                        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("ID_NUM", idNum)
                    editor.apply()
                    // OTP login successful, redirect to ChangePasswordActivity
                    startActivity(Intent(this@LoginActivity, ChangePasswordActivity::class.java))
                } else {
                    // Attempt subsequent login
                    val changePasswordRequest = ChangePassword(idNum, password)
                    apiService.login(changePasswordRequest).enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ) {
                            if (response.isSuccessful) {
                                // Successful login, get user type and redirect accordingly
                                apiService.getUserType(userId)
                                    .enqueue(object : Callback<UserTypeResponse> {
                                        override fun onResponse(
                                            call: Call<UserTypeResponse>,
                                            response: Response<UserTypeResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                // get the type of user
                                                val userType = response.body()?.typeOfUser

                                                // store the idNum and userType of the user so that it can be used in whole app
                                                val sharedPreferences = getSharedPreferences(
                                                    "MyPreferences",
                                                    Context.MODE_PRIVATE
                                                )
                                                val editor = sharedPreferences.edit()
                                                editor.putString("ID_NUM", idNum)
                                                editor.putString("USER_ROLE", userType)
                                                editor.apply()

                                                Log.d("userType", userType ?: "null")
                                                Log.d("userID", userId.idNum)

                                                // Register Pushy & Save Token
                                                Thread {
                                                    try {
                                                        val deviceToken =
                                                            Pushy.register(this@LoginActivity)
                                                        Log.d("FirebaseWrite", deviceToken)
                                                        // Save token under correct user
                                                        FirebaseDatabase.getInstance()
                                                            .getReference("tokens")
                                                            .child(userId.idNum)
                                                            .setValue(deviceToken)

                                                        // Log token for debugging
                                                        println("Pushy Device Token: $deviceToken")
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        Log.e("Error", e.toString())
                                                    }
                                                }.start()

                                                Pushy.listen(this@LoginActivity) // Start receiving push messages

                                                when (userType) {
                                                    "Student" -> {
                                                        finish()
                                                        startActivity(
                                                            Intent(
                                                                this@LoginActivity,
                                                                StudentMainActivity::class.java
                                                            )
                                                        )
                                                    }

                                                    "Lecturer" -> {
                                                        finish()
                                                        startActivity(
                                                            Intent(
                                                                this@LoginActivity,
                                                                LecturerMainActivity::class.java
                                                            )
                                                        )
                                                    }

                                                    else -> Toast.makeText(
                                                        this@LoginActivity,
                                                        "Unknown user type.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Failed to get user type.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e(
                                                    "API_ERROR",
                                                    "Error: ${response.code()}, ${
                                                        response.errorBody()?.string()
                                                    }"
                                                )
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<UserTypeResponse>,
                                            t: Throwable
                                        ) {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                "Failed to get user type.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e(
                                                "API_FAILURE",
                                                "Failed to make request: ${t.message}"
                                            )
                                        }
                                    })
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Invalid credentials.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(
                                    "API_ERROR",
                                    "Error: ${response.code()}, ${response.errorBody()?.string()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Toast.makeText(this@LoginActivity, "Login failed.", Toast.LENGTH_SHORT)
                                .show()
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

    // Function to request notification permission for Android 13+.
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    // Function to create a notification channel for Android 8+.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Pushy notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("default", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}