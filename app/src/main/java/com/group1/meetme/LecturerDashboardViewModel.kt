package com.group1.meetme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

// Handles logic related to lecturer-specific dashboard data like appointment counts.
class LecturerDashboardViewModel(private val lecturerId: String) : ViewModel() {
    // Reference to the root of Firebase Realtime Database
    private val databaseRef = FirebaseDatabase.getInstance().reference

    // Welcome text to be displayed in the UI (can be made dynamic later)
    private val _welcomeText = MutableLiveData("Welcome, Lecturer!")
    val welcomeText: LiveData<String> get() = _welcomeText

    // LiveData to store the count of upcoming appointments for the lecturer
    private val _upcomingAppointmentsCount = MutableLiveData(0)
    val upcomingAppointmentsCount: LiveData<Int> get() = _upcomingAppointmentsCount

    // Called automatically when ViewModel is initialized
    init {
        fetchAppointmentCount()
    }

    // Fetches the number of upcoming appointments from Firebase
    private fun fetchAppointmentCount() {
        // If lecturerId is blank, exit early to avoid Firebase error
        if (lecturerId.isBlank()) return

        // Path: appointmentsLecturer/{lecturerId}
        val appointmentsRef = databaseRef.child("appointmentsLecturer").child(lecturerId)

        // Listen for real-time updates to appointment data
        appointmentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0

                // Loop through each appointment under this lecturer
                for (appointmentSnapshot in snapshot.children) {
                    // Get the status field of the appointment
                    val status = appointmentSnapshot.child("status").getValue(String::class.java)

                    // Only count appointments with status "upcoming"
                    if (status == "upcoming") {
                        count++
                    }
                }
                // Update LiveData with the new count
                _upcomingAppointmentsCount.value = count
            }

            override fun onCancelled(error: DatabaseError) {
                // Log Firebase error for debugging purposes
                println("ERROR: Firebase cancelled - ${error.message}")
            }
        })
    }
}
