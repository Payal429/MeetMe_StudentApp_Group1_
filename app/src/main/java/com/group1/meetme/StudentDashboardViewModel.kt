package com.group1.meetme

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*
import com.group1.meetme.StudentDashboardViewModel

// Responsible for retrieving and managing UI-related data such as welcome text and the count of upcoming appointments from Firebase.
class StudentDashboardViewModel(private val userId: String) : ViewModel() {
    // Reference to the root node in Firebase Realtime Database
    private val databaseRef = FirebaseDatabase.getInstance().reference

    // Mutable LiveData for welcome message shown on the dashboard
    private val _welcomeText = MutableLiveData("Welcome to MeetMe!")
    val welcomeText: LiveData<String> get() = _welcomeText

    // Mutable LiveData to track the number of upcoming appointments
    private val _upcomingAppointmentsCount = MutableLiveData(0)
    val upcomingAppointmentsCount: LiveData<Int> get() = _upcomingAppointmentsCount

    // Called immediately when the ViewModel is created
    init {
        fetchAppointmentCount()
    }

    // Fetches the count of "upcoming" appointments for the current student user from the Firebase Realtime Database under the "appointments" node.
    private fun fetchAppointmentCount() {
        // Skip if userId is empty to avoid database access errors
        if (userId.isBlank()) return

        // Reference to appointments for the current student
        val appointmentsRef = databaseRef.child("appointments").child(userId)

        // Listen for any changes to the student's appointments
        appointmentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                // Iterate through all child appointments
                for (appointmentSnapshot in snapshot.children) {
                    // Retrieve the status value from each appointment
                    val status = appointmentSnapshot.child("status").getValue(String::class.java)
                    // Count only those with status "upcoming"
                    if (status == "upcoming") {
                        count++
                    }
                    // Debug log for individual statuses (optional for development)
                    println("DEBUG: Found appointment with status = $status")
                }
                // Update the LiveData with the final count
                _upcomingAppointmentsCount.value = count
                // Debug log for final result
                println("DEBUG: Upcoming appointment count = $count")
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error message when Firebase fails to retrieve data
                println("ERROR: Firebase cancelled - ${error.message}")
            }
        })
    }
}