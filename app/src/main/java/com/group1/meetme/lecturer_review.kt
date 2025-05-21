package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group1.meetme.BookAppointmentActivity

class lecturer_review : AppCompatActivity() {

    // Lecturer
    private lateinit var lecturerSpinner: Spinner
    private val lecturerNames = mutableListOf<String>()
    private val lecturerIdMap = mutableMapOf<String, String>() // name -> id



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_lecturer_review)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener(){
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        lecturerSpinner = findViewById(R.id.lecturerSpinner)
        loadLecturers()
    }

    private fun loadLecturers() {
        val lecturersRef = FirebaseDatabase.getInstance().reference.child("users").child("Lecturer")

        lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerNames.clear()
                lecturerIdMap.clear()

                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(User::class.java)
                    lecturer?.let {
                        val fullName = "${it.name} ${it.surname}"
                        lecturerNames.add(fullName)
                        lecturerIdMap[fullName] = it.idNum
                    }
                }

                val adapter = ArrayAdapter(this@lecturer_review, android.R.layout.simple_spinner_item, lecturerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lecturerSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@lecturer_review, "Failed to load lecturers", Toast.LENGTH_SHORT).show()
            }
        })
    }
}