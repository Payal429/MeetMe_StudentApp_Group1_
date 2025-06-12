package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group1.meetme.BookAppointmentActivity
import java.util.Locale

// Activity for submitting a review for a lecturer.
class lecturer_review : AppCompatActivity() {

    //Lecturer
    //    private lateinit var lecturerSpinner: Spinner
    //    private val lecturerNames = mutableListOf<String>()
    //    private val lecturerIdMap = mutableMapOf<String, String>() // name -> id

    // UI components.
    private lateinit var ratingBar: RatingBar
    private lateinit var commentEditText: EditText
    private lateinit var submitButton: Button

    // Firebase database reference.
    private lateinit var database: DatabaseReference

    // Data passed from the previous activity.
    private var appointmentId: String? = null
    private var studentId: String? = null
    private var lecturerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lecturer_review)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components.
        ratingBar = findViewById(R.id.ratingBar)
        commentEditText = findViewById(R.id.commentEditText)
        submitButton = findViewById(R.id.submitReviewButton)

        // Initialize Firebase database reference.
        database = FirebaseDatabase.getInstance().reference

        // Retrieve data passed from the previous activity.
        appointmentId = intent.getStringExtra("appointmentId")
        studentId = intent.getStringExtra("studentId")
        lecturerId = intent.getStringExtra("lecturerId")

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish() // Optional: finishes current activity so it's removed from back stack
        }

        // Disable button initially
        submitButton.isEnabled = false

        // Check if review already exists
        if (appointmentId != null) {
            database.child("reviews").child(appointmentId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Show a toast if a review already exists and close the activity.
                            Toast.makeText(
                                this@lecturer_review,
                                "You already submitted a review for this appointment.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Close the activity
                        } else {
                            submitButton.isEnabled = true
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@lecturer_review,
                            "Error checking review: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                })
        }

        // Set up the submit button click listener.
        submitButton.setOnClickListener {
            // Get the rating and comment from the UI components.
            val rating = ratingBar.rating
            val comment = commentEditText.text.toString().trim()

            // Check if the rating is given.
            if (rating == 0f) {
                Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a Review object with the provided data.
            val review = Review(
                appointmentId = appointmentId!!,
                studentId = studentId!!,
                lecturerId = lecturerId!!,
                rating = rating,
                comment = comment
            )

            // Save the review to the Firebase database.
            database.child("reviews").child(appointmentId!!).setValue(review).addOnSuccessListener {
                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
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
}

//        // Return back to the dashboard
//        val backArrow: ImageButton = findViewById(R.id.backArrow)
//
//        // Return back to the dashboard when button is clicked
//        backArrow.setOnClickListener(){
//            val intent = Intent(this, StudentDashboardActivity::class.java)
//            startActivity(intent)
//        }
//
//        lecturerSpinner = findViewById(R.id.lecturerSpinner)
//        loadLecturers()
//    }
//
//    private fun loadLecturers() {
//        val lecturersRef = FirebaseDatabase.getInstance().reference.child("users").child("Lecturer")
//
//        lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                lecturerNames.clear()
//                lecturerIdMap.clear()
//
//                for (lecturerSnap in snapshot.children) {
//                    val lecturer = lecturerSnap.getValue(User::class.java)
//                    lecturer?.let {
//                        val fullName = "${it.name} ${it.surname}"
//                        lecturerNames.add(fullName)
//                        lecturerIdMap[fullName] = it.idNum
//                    }
//                }
//
//                val adapter = ArrayAdapter(this@lecturer_review, android.R.layout.simple_spinner_item, lecturerNames)
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                lecturerSpinner.adapter = adapter
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@lecturer_review, "Failed to load lecturers", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//}