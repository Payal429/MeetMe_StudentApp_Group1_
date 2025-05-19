package com.group1.meetme

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext

class AppointmentAdapter(
    private val context: Context,
    private val appointments: List<Appointment>,
    private var userType: String, // "student" or "lecturer"
    private val onAction1: ((Appointment) -> Unit)? = null,
    private val onAction2: ((Appointment) -> Unit)? = null
) : BaseAdapter() {

    override fun getCount(): Int = appointments.size
    override fun getItem(position: Int): Any = appointments[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_appointment, parent, false)

        val appointment = appointments[position]

        // Get user ID and type from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
       val userId = sharedPreferences.getString("ID_NUM", null)
       val  userRole = sharedPreferences.getString("USER_ROLE", null)!!
        Log.d("userId", userId!!)
        Log.d("userType", userRole!!)


        val infoText = view.findViewById<TextView>(R.id.infoText)
        val actionLayout = view.findViewById<LinearLayout>(R.id.actionButtons)
        val btn1 = view.findViewById<Button>(R.id.btnAction1)
        val btn2 = view.findViewById<Button>(R.id.btnAction2)

        // Set info text based on user type
        infoText.text = if (userRole == "Student") {
            "Lecturer: ${appointment.lecturerId}\n" +
                    "Date: ${appointment.date} ${appointment.time}\n" +
                    "Module: ${appointment.module}"
        } else {
            "Student: ${appointment.id}\n" +
                    "Date: ${appointment.date} ${appointment.time}\n" +
                    "Module: ${appointment.module}"
        }

        // Show/hide action buttons based on user type and appointment status
        if (userRole == "Student") {
            when (appointment.status) {
                "upcoming" -> {
                    actionLayout.visibility = View.VISIBLE
                    btn1.text = "Cancel"
                    btn2.text = "Reschedule"
                    btn1.setOnClickListener { onAction1?.invoke(appointment) }
                    btn2.setOnClickListener { onAction2?.invoke(appointment) }
                }
                "completed" -> {
                    actionLayout.visibility = View.VISIBLE
//                    btn1.text = "Rebook"
                    btn1.visibility = View.GONE
                    btn2.text = "Review"
                    btn1.setOnClickListener { onAction1?.invoke(appointment) }
                    btn2.setOnClickListener { onAction2?.invoke(appointment) }
                }
                "cancelled" -> {
                    actionLayout.visibility = View.GONE
                }
            }
        } else {
            // Lecturer: Hide buttons
            actionLayout.visibility = View.GONE
        }

        return view
    }
}
