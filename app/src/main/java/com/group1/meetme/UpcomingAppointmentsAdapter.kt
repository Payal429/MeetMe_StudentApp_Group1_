package com.group1.meetme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// Adapter class to display a list of upcoming appointments in a RecyclerView
class UpcomingAppointmentsAdapter(
    // List of appointments to display
    private val appointments: List<Appointment>,
    // User type ("Student" or "Lecturer")
    private val userType: String) : RecyclerView.Adapter<UpcomingAppointmentsAdapter.ViewHolder>() {

    // ViewHolder holds references to the UI components of each item row
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // TextView to show Lecturer
        val LecturerTV: TextView = view.findViewById(R.id.LecturerTV)
        // TextView to show date and time
        val DateTimeVenueTV: TextView = view.findViewById(R.id.DateTimeVenueTV)
        // TextView to show Venue
        val VenueTV: TextView = view.findViewById(R.id.VenueName)
        // TextView to show module name
        val ModuleName: TextView = view.findViewById(R.id.ModuleName)
    }

    // Inflate the layout for each appointment item and create a ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_appointment, parent, false)
        return ViewHolder(view)
    }

    // Bind data from an Appointment object to the views inside the ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]

        // Fetch and display the full name based on userType and appointment data
        if (userType == "Student") {
            // If current user is a student, show the lecturer's full name
            fetchFullNameById(appointment.lecturerId ?: "", "Lecturer") { fullName ->
                holder.LecturerTV.text = "Lecturer: $fullName"
            }
        } else {
            // If current user is a lecturer, show the student's full name
            fetchFullNameById(appointment.studentID ?: "", "Student") { fullName ->
                holder.LecturerTV.text = "Student: $fullName"
            }
        }

        // Set the date and time text (e.g., "2024-06-01 at 15:00")
        holder.DateTimeVenueTV.text = "${appointment.date} at ${appointment.time}"

        // Set the venue text (e.g., "Online")
        holder.VenueTV.text = "${appointment.venue} "

        // Display the module name for the appointment
        holder.ModuleName.text = "${appointment.module}"
    }

    // Return the total number of appointment items in the list
    override fun getItemCount() = appointments.size

    // Helper method to fetch full name (name + surname) from Firebase based on user ID and role
    private fun fetchFullNameById(id: String, role: String, callback: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(role)
            .child(id)

        // Attach a single value listener to get user data once
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Read 'name' and 'surname' from the database snapshot
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                    // Return the full name via callback
                    callback("$name $surname")
                } else {
                    // User data not found in DB
                    callback("Unknown $role")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error by returning error message
                callback("Error loading name")
            }
        })
    }
}
