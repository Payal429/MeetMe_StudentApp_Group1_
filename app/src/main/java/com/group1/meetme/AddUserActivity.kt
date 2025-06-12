package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.group1.meetme.databinding.ActivityAddUserBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

//  This activity is responsible for adding a new user to the system.
// It provides a form for inputting user details and handles the logic for
// writing the user data to Firebase and making an API call to onboard the user.

class AddUserActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivityAddUserBinding

    // Retrofit API service instance for making network requests.
    private val apiService: ApiService = ApiClient.create(ApiService::class.java)
    // Firebase database reference for storing user data.
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_add_user)
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the back arrow button by its ID.
//        val backArrow: ImageButton = findViewById(R.id.backArrow)
        // Set a click listener for the back arrow button to navigate back to the dashboard.
        binding.backArrow.setOnClickListener(){
//            val intent = Intent(this, StudentDashboardActivity::class.java)
//            startActivity(intent)
            finish()
        }
//        val employeeId = binding.loginScreenEditEmpNumPlainText.text.toString()
//        val password = binding.loginScreenEditPasswordPlainText.text.toString()
//        login(employeeId, password)

        // Find the UI elements buttons by their IDs
//        val edtIDNumber: EditText = findViewById(R.id.edtIDNumber)
//        val edtName: EditText = findViewById(R.id.edtName)
//        val edtSurname: EditText = findViewById(R.id.edtSurname)
//        val edtUser: EditText = findViewById(R.id.edtUser)
//        val spnCourse: Spinner = findViewById(R.id.spnCourse)
//        val edtEmail: EditText = findViewById(R.id.edtEmail)
//        val addStudentButton: Button = findViewById(R.id.addStudentButton)
//        val headerTitle: TextView = findViewById(R.id.headerTitle)

        // Get the user type passed from the previous activity.
        val userType = getIntent().getStringExtra("userType")
        //edtUser.setText(userType)

        // Set the header title based on the user type.
        binding.headerTitle.text = "Add New $userType"
        // Set the user type in the edtUser EditText.
        binding.edtUser.setText(userType)
        // Initialize the Firebase database reference
        database = Firebase.database.reference

        // Get the user details from the input fields.
        binding.addStudentButton.setOnClickListener {
            val idNum = binding.edtIDNumber.text.toString()
            val name = binding.edtName.text.toString()
            val surname = binding.edtSurname.text.toString()
            val typeOfUser = binding.edtUser.text.toString()
            val course = binding.spnCourse.selectedItem.toString()
            val email = binding.edtEmail.text.toString()

            // Call the onboardUser function to onboard the user via API.
            onboardUser(idNum, name, surname, typeOfUser, course, email)

            // Check the user type and write the user data to Firebase accordingly.
            if (userType =="Student"){
                writeNewUserStudent(idNum, name, surname, typeOfUser, course, email)
            } else if (userType == "Lecturer"){
                writeNewUserLecturer(idNum, name, surname, typeOfUser, course, email)
            }
        }
    }

    // Write a new student user to the Firebase database.
    fun writeNewUserStudent(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        val user = User(idNum, name, surname, typeOfUser, course, email)
        database.child("users").child("Student").child(idNum).setValue(user)
    }

    // Write a new lecturer user to the Firebase database.
    fun writeNewUserLecturer(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        val user = User(idNum, name, surname, typeOfUser, course, email)
        database.child("users").child("Lecturer").child(idNum).setValue(user)
    }

    // This method will access the login api, and will check to see if the entered details are correct, and will log the user in to the app
    private fun onboardUser(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        // Create an OnboardRequest object with the user details.
        val onboardRequest = OnboardRequest(idNum, name, surname, typeOfUser, course, email)

        // Make an API call to onboard the new user.
        apiService.onboardNewUser(onboardRequest).enqueue(object : Callback<ApiResponse> {

            // Handle the API response.
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful && response.body()?.message == "User Successfully onboarded") {
                    Toast.makeText(this@AddUserActivity, "User Successfully onboarded!!", Toast.LENGTH_SHORT).show()
//                }
            }
            // Handle API request failure.
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@AddUserActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_FAILURE", "Failed to make request: ${t.message}")
            }
        })
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
}