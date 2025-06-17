package com.group1.meetme

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.group1.meetme.databinding.ActivityManageUsersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ManageUsersActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding: ActivityManageUsersBinding

    // Initialize the ApiService using the ApiClient.
    private val apiService: ApiService = ApiClient.create(ApiService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the view using ViewBinding and set it as the content view
        binding = ActivityManageUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up back arrow click listener to close this activity and go back
        binding.backArrow.setOnClickListener() {
            finish()
        }

        // Set up click listener for "Resend OTP" button
        binding.resendOtpButton.setOnClickListener {
            val idNum = binding.idNumEditText.text.toString().trim()

            // Show message if ID number field is empty
            if (idNum.isEmpty()) {
                Toast.makeText(this, "Please enter a user ID number", Toast.LENGTH_SHORT).show()
            } else {
                // Call the function to resend OTP with the entered ID number
                resendOtp(idNum)
            }
        }
        // Load saved language preference
        loadLanguage()
    }

    // Loads language preference from SharedPreferences and sets locale
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        // Default to English
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Configures the app locale/language based on the language code
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        // Set it as default
        Locale.setDefault(locale)
        // Apply the locale to app configuration
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Sends a network request to the backend to resend OTP for the given ID number
    private fun resendOtp(idNum: String) {
        // Create the request body with key "idNum"
        val body = mapOf("idNum" to idNum)

        // Use Retrofit to enqueue an async HTTP POST request
        apiService.resendOtp(body).enqueue(object : Callback<Map<String, String>> {
            // Called when the server responds (either success or failure)
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    // Extract success message from response or use fallback
                    val message = response.body()?.get("message") ?: "OTP resent."
                    binding.statusTextView.text = message
                } else {
                    // Response was not successful (e.g., 400 or 500 HTTP code)
                    binding.statusTextView.text = "Failed to resend OTP. Please try again."
                }
            }

            // Called when the network request fails entirely (e.g., no internet)
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                // Show a generic error message with the exception's localized description
                binding.statusTextView.text = "Network error: ${t.localizedMessage}"
            }
        })
    }
}