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

class UpcomingAppointmentsAdapter(
    private val appointments: List<Appointment>,
    private val userType: String
) : RecyclerView.Adapter<UpcomingAppointmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val LecturerTV: TextView = view.findViewById(R.id.LecturerTV)
        val DateTimeVenueTV: TextView = view.findViewById(R.id.DateTimeVenueTV)
        val ModuleName: TextView = view.findViewById(R.id.ModuleName)
//        val timeText: TextView = view.findViewById(R.id.timeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
//        holder.LecturerTV.text = if (userType == "Student") "Lecturer: ${appointment.lecturerId}" else "Student: ${appointment.studentID}"
        if (userType == "Student") {
            fetchFullNameById(appointment.lecturerId ?: "", "Lecturer") { fullName ->
                holder.LecturerTV.text = "Lecturer: $fullName"
            }
        } else {
            fetchFullNameById(appointment.studentID ?: "", "Student") { fullName ->
                holder.LecturerTV.text = "Student: $fullName"
            }
        }

        holder.DateTimeVenueTV.text = "${appointment.date} at ${appointment.time}"
        holder.ModuleName.text = "${appointment.module}"
    }

    override fun getItemCount() = appointments.size

    private fun fetchFullNameById(id: String, role: String, callback: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(role)
            .child(id)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val surname = snapshot.child("surname").getValue(String::class.java) ?: ""
                    callback("$name $surname")
                } else {
                    callback("Unknown $role")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback("Error loading name")
            }
        })
    }

}
