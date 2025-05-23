package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

class lecturer_review : AppCompatActivity() {

//    // Lecturer
//    private lateinit var lecturerSpinner: Spinner
//    private val lecturerNames = mutableListOf<String>()
//    private val lecturerIdMap = mutableMapOf<String, String>() // name -> id
    private lateinit var ratingBar: RatingBar
    private lateinit var commentEditText: EditText
    private lateinit var submitButton: Button

    private lateinit var database: DatabaseReference
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

        ratingBar = findViewById(R.id.ratingBar)
        commentEditText = findViewById(R.id.commentEditText)
        submitButton = findViewById(R.id.submitReviewButton)

        database = FirebaseDatabase.getInstance().reference

        appointmentId = intent.getStringExtra("appointmentId")
        studentId = intent.getStringExtra("studentId")
        lecturerId = intent.getStringExtra("lecturerId")

        // Disable button initially
        submitButton.isEnabled = false

        // Check if review already exists
        if (appointmentId != null) {
            database.child("reviews").child(appointmentId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@lecturer_review, "You already submitted a review for this appointment.", Toast.LENGTH_LONG).show()
                        finish() // Close the activity
                    } else {
                        submitButton.isEnabled = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@lecturer_review, "Error checking review: ${error.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        }

        submitButton.setOnClickListener {
            val rating = ratingBar.rating
            val comment = commentEditText.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(this, "Please give a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val review = Review(
                appointmentId = appointmentId!!,
                studentId = studentId!!,
                lecturerId = lecturerId!!,
                rating = rating,
                comment = comment
            )

            database.child("reviews").child(appointmentId!!).setValue(review).addOnSuccessListener {
                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show()
            }
        }
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