package com.group1.meetme

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageUsersActivity : AppCompatActivity() {

    private lateinit var idNumEditText: EditText
    private lateinit var resendOtpButton: Button
    private lateinit var statusTextView: TextView

    // Initialize the ApiService using the ApiClient.
    private val apiService: ApiService = ApiClient.create(ApiService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_users)

        idNumEditText = findViewById(R.id.idNumEditText)
        resendOtpButton = findViewById(R.id.resendOtpButton)
        statusTextView = findViewById(R.id.statusTextView)

        // Find the back arrow button by its ID.
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        // Set a click listener for the back arrow button to navigate back to the dashboard.
        backArrow.setOnClickListener(){
//            val intent = Intent(this, StudentDashboardActivity::class.java)
//            startActivity(intent)
            finish()
        }

        resendOtpButton.setOnClickListener {
            val idNum = idNumEditText.text.toString().trim()

            if (idNum.isEmpty()) {
                Toast.makeText(this, "Please enter a user ID number", Toast.LENGTH_SHORT).show()
            } else {
                resendOtp(idNum)
            }
        }
    }

    private fun resendOtp(idNum: String) {
        val body = mapOf("idNum" to idNum)

        apiService.resendOtp(body).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    val message = response.body()?.get("message") ?: "OTP resent."
                    statusTextView.text = message
                } else {
                    statusTextView.text = "Failed to resend OTP. Please try again."
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                statusTextView.text = "Network error: ${t.localizedMessage}"
            }
        })
    }
}