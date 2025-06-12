package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.group1.meetme.databinding.ActivityChangePasswordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

// Activity for changing the user's password.
class ChangePasswordActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivityChangePasswordBinding

    // Initialize the ApiService using the ApiClient.
    private val apiService: ApiService = ApiClient.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge support for better visual experience.
        enableEdgeToEdge()
        // Set the content view to the activity_change_password layout.
//        setContentView(R.layout.activity_change_password)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the password input field and the change password button.
//        val passwordEditText: TextInputEditText = findViewById(R.id.edtChangePassword)
//        val btnChangePassword: Button = findViewById(R.id.btnChangePassword)

        // Set up the button click listener to handle password change.
        binding.btnChangePassword.setOnClickListener {
            //val employeeId = intent.getStringExtra("EMPLOYEE_ID") // Pass from LoginActivity
            val newPassword = binding.edtChangePassword.text.toString()
//            val employeeId = intent.getStringExtra("EMPLOYEE_ID")

            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val idNum = sharedPreferences.getString("ID_NUM", null)

            // Check if the ID number is available.
            if (idNum != null) {
                Log.d("ID Num", idNum)
                changePassword(idNum ?: "", newPassword)
            } else {
                Log.d("ID Num", "ID Num is null")
            }
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

    // This method will call the API to change the password to the value that the user has entered
    private fun changePassword(idNum: String, newPassword: String) {
        val changePasswordRequest = ChangePassword(idNum, newPassword)

        // Enqueue the API call to change the password.
        apiService.changePassword(changePasswordRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Password changed successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@ChangePasswordActivity, LoginActivity::class.java))
                } else {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Failed to change password.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "API_ERROR",
                        "Error: ${response.code()}, ${response.errorBody()?.string()}"
                    )
                }
            }

            // Handle any failure in making the API request.
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(
                    this@ChangePasswordActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("API_FAILURE", "Failed to make request: ${t.message}")
            }
        })
    }
}