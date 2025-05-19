package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.log

/**
 * A simple [Fragment] subclass.
 * Use the [UpcomingAppointmentsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpcomingAppointmentsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private val appointments = mutableListOf<Appointment>()
    private lateinit var adapter: AppointmentAdapter

    private var userId: String? = ""
    private var userType: String? = ""

    companion object {
        fun newInstance(userId: String, userType: String): UpcomingAppointmentsFragment {
            val fragment = UpcomingAppointmentsFragment()
            val args = Bundle()
            args.putString("USER_ID", userId)
            args.putString("USER_ROLE", userType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upcoming_appointments, container, false)
        listView = view.findViewById(R.id.appointmentsListView)
        database = FirebaseDatabase.getInstance().reference

//        // Get user ID and type from SharedPreferences
//        val sharedPreferences = context?.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
//        userId = sharedPreferences?.getString("ID_NUM", null)
//        userType = sharedPreferences?.getString("USER_TYPE", null)
//        Log.d("userId------", userId!!)
//        Log.d("userType------", userType!!)

        super.onCreate(savedInstanceState)
        userId = arguments?.getString("USER_ID")
        userType = arguments?.getString("USER_ROLE")
        Log.d("FragmentArgs", "userId = $userId, userType = $userType")

        if (userId.isNullOrEmpty() || userType.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User info not found", Toast.LENGTH_SHORT).show()
            return view
        }


        adapter = AppointmentAdapter(
            requireContext(),
            appointments,
            userType = userType!!,
            onAction1 = { appointment -> cancelAppointment(appointment) },
            onAction2 = { appointment -> rescheduleAppointment(appointment) }
        )
        listView.adapter = adapter

        loadAppointments()
        return view
    }

    private fun loadAppointments() {
        val currentDateTime = Calendar.getInstance()

        val rolePath = if (userType == "Lecturer") "lecturerId" else "studentId"

        database.child("appointments").child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointments.clear()

                    for (snap in snapshot.children) {
                        val appointment = snap.getValue(Appointment::class.java)
                        val key = snap.key ?: continue
                        appointment?.id = key

                        if (appointment != null) {
                            val appointmentDateTime = getAppointmentDateTime(appointment.date, appointment.time)
                            Log.d("appDate", appointment.date)
                            Log.d("appTime", appointment.time)
                            Log.d("appStatus", appointment.status)
                            Log.d("appid", appointment.id)

                            if (appointmentDateTime.before(currentDateTime.time)) {
                                if (appointment.status == "upcoming") {
                                    // Mark past "upcoming" appointments as "completed"
                                    database.child("appointments")
                                        .child(userId!!)
                                        .child(key)
                                        .child("status")
                                        .setValue("completed")
                                }
                            } else if (appointment.status == "upcoming") {
                                appointments.add(appointment)
                            }
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Appointments", "Failed to load appointments: ${error.message}")
                }
            })
    }

    private fun getAppointmentDateTime(dateStr: String, timeStr: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.parse("$dateStr $timeStr") ?: Date(0)
    }

    private fun cancelAppointment(appointment: Appointment) {
        Log.d("cancelAppointment: ", appointment.toString())
//        if (userType != "Student") return
//        val intent = Intent(requireContext(), CancelAppointmentActivity::class.java).apply {
//            putExtra("appointmentId", appointment.id)
//            putExtra("studentId", userId)
//        }
//        startActivity(intent)
    }

    private fun rescheduleAppointment(appointment: Appointment) {
        if (userType != "Student") return
        val intent = Intent(requireContext(), RescheduleActivity::class.java).apply {
            putExtra("appointmentId", appointment.id)
            putExtra("studentId", userId)
            putExtra("lecturerId", appointment.lecturerId)
            putExtra("oldDate", appointment.date)
            putExtra("oldTime", appointment.time)
        }
        startActivity(intent)
    }
}