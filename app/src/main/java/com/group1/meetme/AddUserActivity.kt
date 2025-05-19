package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddUserActivity : AppCompatActivity() {

    private val apiService: ApiService = ApiClient.create(ApiService::class.java)

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val employeeId = binding.loginScreenEditEmpNumPlainText.text.toString()
//        val password = binding.loginScreenEditPasswordPlainText.text.toString()
//        login(employeeId, password)

        // Find the buttons by their IDs
        val edtIDNumber: EditText = findViewById(R.id.edtIDNumber)
        val edtName: EditText = findViewById(R.id.edtName)
        val edtSurname: EditText = findViewById(R.id.edtSurname)
        val edtUser: EditText = findViewById(R.id.edtUser)
        val spnCourse: Spinner = findViewById(R.id.spnCourse)
        val edtEmail: EditText = findViewById(R.id.edtEmail)
        val addStudentButton: Button = findViewById(R.id.addStudentButton)
        val headerTitle: TextView = findViewById(R.id.headerTitle)

        val userType = getIntent().getStringExtra("userType")
        //edtUser.setText(userType)
        headerTitle.text = "Add New $userType"
        edtUser.setText(userType)

        database = Firebase.database.reference



        addStudentButton.setOnClickListener {
            val idNum = edtIDNumber.text.toString()
            val name = edtName.text.toString()
            val surname = edtSurname.text.toString()
            val typeOfUser = edtUser.text.toString()
            val course = spnCourse.selectedItem.toString()
            val email = edtEmail.text.toString()
            onboardUser(idNum, name, surname, typeOfUser, course, email)

            if (userType =="Student"){
                writeNewUserStudent(idNum, name, surname, typeOfUser, course, email)
            } else if (userType == "Lecturer"){
                writeNewUserLecturer(idNum, name, surname, typeOfUser, course, email)
            }


        }
    }

    fun writeNewUserStudent(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        val user = User(idNum, name, surname, typeOfUser, course, email)
        database.child("users").child("Student").child(idNum).setValue(user)
    }

    fun writeNewUserLecturer(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        val user = User(idNum, name, surname, typeOfUser, course, email)
        database.child("users").child("Lecturer").child(idNum).setValue(user)
    }

    //this method will access the login api, and will check to see if the entered details are correct, and will log the user in to the app
    private fun onboardUser(idNum: String, name: String, surname: String, typeOfUser: String, course: String, email: String) {
        val onboardRequest = OnboardRequest(idNum, name, surname, typeOfUser, course, email)

        apiService.onboardNewUser(onboardRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful && response.body()?.message == "User Successfully onboarded") {
                    Toast.makeText(this@AddUserActivity, "User Successfully onboarded!!", Toast.LENGTH_SHORT).show()
//                }
            }
            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@AddUserActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("API_FAILURE", "Failed to make request: ${t.message}")
            }
        })
    }
}