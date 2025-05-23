package com.group1.meetme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        holder.LecturerTV.text = if (userType == "Student") "Lecturer: ${appointment.lecturerId}" else "Student: ${appointment.studentID}"
        holder.DateTimeVenueTV.text = "${appointment.date} at ${appointment.time}"
        holder.ModuleName.text = "${appointment.module}"
    }

    override fun getItemCount() = appointments.size
}
