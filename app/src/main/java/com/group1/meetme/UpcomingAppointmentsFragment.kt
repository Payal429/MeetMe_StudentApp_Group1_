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
import com.google.android.gms.common.server.response.FastParser.ParseException
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

// A simple [Fragment] subclass. Use the [UpcomingAppointmentsFragment.newInstance] factory method to  create an instance of this fragment.
class UpcomingAppointmentsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private val appointments = mutableListOf<Appointment>()
    private lateinit var adapter: AppointmentAdapter

    private var userId: String? = ""
    private var userType: String? = ""

    // Companion object to create a new instance of the fragment with user information.
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
        // Inflate the fragment layout.
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

        // Get user ID and type from the arguments passed to the fragment.
        userId = arguments?.getString("USER_ID")
        userType = arguments?.getString("USER_ROLE")
        Log.d("FragmentArgs", "userId = $userId, userType = $userType")

        // Check if user information is available.
        if (userId.isNullOrEmpty() || userType.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User info not found", Toast.LENGTH_SHORT).show()
            return view
        }

        // Initialize the adapter for the list of appointments.
        adapter = AppointmentAdapter(
            requireContext(),
            appointments,
            userType = userType!!,
            onAction1 = { appointment -> cancelAppointment(appointment) },
            onAction2 = { appointment -> rescheduleAppointment(appointment) }
        )
        listView.adapter = adapter

        // Load upcoming appointments.
        loadAppointments()
        return view
    }

//    private fun loadAppointments() {
//        val currentDateTime = Calendar.getInstance()
//
//        val rolePath = if (userType == "Lecturer") "lecturerId" else "studentId"
//
//        database.child("appointments").child(userId!!)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    appointments.clear()
//
//                    for (snap in snapshot.children) {
//                        val appointment = snap.getValue(Appointment::class.java)
//                        val key = snap.key ?: continue
//                        appointment?.id = key
//
//                        if (appointment != null) {
//                            val appointmentDateTime = getAppointmentDateTime(appointment.date, appointment.time)
//                            Log.d("appDate", appointment.date)
//                            Log.d("appTime", appointment.time)
//                            Log.d("appStatus", appointment.status)
//                            Log.d("appid", appointment.id)
//
//                            if (appointmentDateTime.before(currentDateTime.time)) {
//                                if (appointment.status == "upcoming") {
//                                    // Mark past "upcoming" appointments as "completed"
//                                    database.child("appointments")
//                                        .child(userId!!)
//                                        .child(key)
//                                        .child("status")
//                                        .setValue("completed")
//                                }
//                            } else if (appointment.status == "upcoming") {
//                                appointments.add(appointment)
//                            }
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("Appointments", "Failed to load appointments: ${error.message}")
//                }
//            })
//    }

    // Function to load upcoming appointments based on user type.
//    private fun loadAppointments() {
//        val currentDateTime = Calendar.getInstance()
//
//        // Clear the list of appointments.
//        appointments.clear()
//
//        if (userType == "Student") {
//            // Load from appointments/{studentId}
//            database.child("appointments").child(userId!!)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        for (snap in snapshot.children) {
//                            val appointment = snap.getValue(Appointment::class.java)
//                            val key = snap.key ?: continue
//                            appointment?.id = key
//
//                            if (appointment != null) {
//                                val appointmentDateTime =
//                                    getAppointmentDateTime(appointment.date, appointment.time)
//
//                                if (appointmentDateTime!!.before(currentDateTime.time)) {
//                                    if (appointment.status == "upcoming") {
//                                        // Auto-mark as completed
//                                        database.child("appointments").child(userId!!).child(key)
//                                            .child("status")
//                                            .setValue("completed")
//                                    }
//                                } else if (appointment.status == "upcoming") {
//                                    appointments.add(appointment)
//                                }
//                            }
//                        }
//                        adapter.notifyDataSetChanged()
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("Appointments", "Student load failed: ${error.message}")
//                    }
//                })
//        } else if (userType == "Lecturer") {
//            // Load from appointmentsLecturer/{lecturerId}
//            database.child("appointmentsLecturer").child(userId!!)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        for (snap in snapshot.children) {
//                            val appointment = snap.getValue(Appointment::class.java)
//                            val key = snap.key ?: continue
//                            appointment?.id = key
//                            appointment?.lecturerId = userId!! // Manually set lecturerId
//
//                            if (appointment != null) {
//                                val appointmentDateTime =
//                                    getAppointmentDateTime(appointment.date, appointment.time)
//
//                                if (appointmentDateTime!!.before(currentDateTime.time)) {
//                                    if (appointment.status == "upcoming") {
//                                        // Auto-mark as completed
//                                        database.child("appointmentsLecturer").child(userId!!)
//                                            .child(key).child("status")
//                                            .setValue("completed")
//                                    }
//                                } else if (appointment.status == "upcoming") {
//                                    appointments.add(appointment)
//                                }
//                            }
//                        }
//                        adapter.notifyDataSetChanged()
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("Appointments", "Lecturer load failed: ${error.message}")
//                    }
//                })
//        }
//    }
    private fun loadAppointments() {
        val currentDateTime = Calendar.getInstance()
        appointments.clear()

        val path = if (userType == "Lecturer") "appointmentsLecturer" else "appointments"
        database.child(path).child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val appointment = snap.getValue(Appointment::class.java)
                        val key = snap.key ?: continue
                        if (appointment == null) continue

                        appointment.id = key
                        if (userType == "Lecturer") {
                            appointment.lecturerId = userId!!
                        }

                        // Get safe datetime
                        val appointmentDateTime = getAppointmentDateTime(appointment.date, appointment.time)
                        if (appointmentDateTime == null) {
                            Log.e("Appointments", "Invalid date/time: '${appointment.date}', '${appointment.time}'")
                            continue
                        }

                        // Mark as completed if in past
                        if (appointmentDateTime.before(currentDateTime.time)) {
                            if (appointment.status == "upcoming") {
                                database.child(path).child(userId!!).child(key).child("status")
                                    .setValue("completed")
                            }
                        } else if (appointment.status == "upcoming") {
                            appointments.add(appointment)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Appointments", "Failed to load appointments: ${error.message}")
                }
            })
    }




//    // Function to get the appointment date and time as a Date object.
//    private fun getAppointmentDateTime(dateStr: String, timeStr: String): Date {
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//        return sdf.parse("$dateStr $timeStr") ?: Date(0)
//    }
private fun getAppointmentDateTime(dateStr: String?, timeStr: String?): Date? {
    if (dateStr.isNullOrBlank() || timeStr.isNullOrBlank()) {
        Log.e("AppointmentParser", "Missing or blank date/time: date='$dateStr', time='$timeStr'")
        return null
    }

    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.parse("$dateStr $timeStr")
    } catch (e: ParseException) {
        Log.e("AppointmentParser", "Failed to parse: '$dateStr $timeStr'", e)
        null
    }
}

    // Function to handle canceling an appointment.
    private fun cancelAppointment(appointment: Appointment) {
        Log.d("cancelAppointment: ", appointment.toString())
        if (userType != "Student") return
        val intent = Intent(requireContext(), CancelAppointmentActivity::class.java).apply {
            putExtra("appointmentId", appointment.id)
            putExtra("studentId", userId)
        }
        startActivity(intent)
    }

    // Function to handle rescheduling an appointment.
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