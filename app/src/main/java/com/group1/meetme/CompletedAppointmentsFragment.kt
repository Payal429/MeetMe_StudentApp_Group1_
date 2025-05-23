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
import com.google.firebase.database.*

// Fragment to display completed appointments.
class CompletedAppointmentsFragment : Fragment() {

    // UI components and data.
    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AppointmentAdapter
    private val appointments = mutableListOf<Appointment>()

    // User information.
    private var userId: String? = null
    private var userType: String? = null

    // Companion object to create a new instance of the fragment with user information.
    companion object {
        fun newInstance(userId: String, userType: String): CompletedAppointmentsFragment {
            val fragment = CompletedAppointmentsFragment()
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
        val view = inflater.inflate(R.layout.fragment_completed_appointments, container, false)
        listView = view.findViewById(R.id.appointmentsListView)
        database = FirebaseDatabase.getInstance().reference

//        // Get user ID and type from SharedPreferences
//        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
//        userId = sharedPreferences.getString("ID_NUM", null)
//        userType = sharedPreferences.getString("USER_TYPE", null)


        super.onCreate(savedInstanceState)
        // Get user ID and role from the arguments passed to the fragment.
        userId = arguments?.getString("USER_ID")
        userType = arguments?.getString("USER_ROLE")
        Log.d("FragmentArgs", "userId = $userId, userType = $userType")

        // Check if user information is available.
        if (userId == null || userType == null) {
            Toast.makeText(requireContext(), "User info not found", Toast.LENGTH_SHORT).show()
            return view
        }

        // Initialize the adapter for the list of appointments
        adapter = AppointmentAdapter(
            requireContext(),
            appointments,
            userType = userType!!,
            // Rebook action for students.
            onAction1 = { appointment -> rebookAppointment(appointment) },
            // Review action for students.
            onAction2 = { appointment -> reviewAppointment(appointment) }
        )
        listView.adapter = adapter

        // Load completed appointments.
        loadCompletedAppointments()
        return view
    }

//    private fun loadCompletedAppointments() {
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
//                        if (appointment != null && appointment.status == "completed") {
//                            appointments.add(appointment)
//                        }
//                    }
//
//                    adapter.notifyDataSetChanged()
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("CompletedAppointments", "Error: ${error.message}")
//                }
//            })
//    }

    // Function to load completed appointments based on user type.
    private fun loadCompletedAppointments() {
        if (userType == "Lecturer") {
            // Lecturer view
            database.child("appointmentsLecturer").child(userId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        appointments.clear()
                        for (snap in snapshot.children) {
                            val appointment = snap.getValue(Appointment::class.java)
                            val key = snap.key ?: continue
                            appointment?.id = key

                            if (appointment != null && appointment.status == "completed") {
                                appointments.add(appointment)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CompletedAppointments", "Lecturer error: ${error.message}")
                    }
                })
        } else {
            // Student view
            database.child("appointments").child(userId!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        appointments.clear()
                        for (snap in snapshot.children) {
                            val appointment = snap.getValue(Appointment::class.java)
                            val key = snap.key ?: continue
                            appointment?.id = key

                            if (appointment != null && appointment.status == "completed") {
                                appointments.add(appointment)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CompletedAppointments", "Student error: ${error.message}")
                    }
                })
        }
    }

    // Function to handle rebooking an appointment.
    private fun rebookAppointment(appointment: Appointment) {
        // Only allow students to rebook appointments.
        if (userType != "Student") return

        // Create an Intent to navigate to the RescheduleActivity.
        val intent = Intent(requireContext(), RescheduleActivity::class.java).apply {
            putExtra("lecturerId", appointment.lecturerId)
            putExtra("module", appointment.module)
            putExtra("isRebooking", true)  // Used to indicate rebooking
        }
        startActivity(intent)
    }

    // Function to handle reviewing an appointment.
    private fun reviewAppointment(appointment: Appointment) {
        if (userType != "Student") return

//         val intent = Intent(requireContext(), RescheduleActivity::class.java).apply {
//            putExtra("appointmentId", appointment.id)
//            putExtra("studentId", userId)
//            putExtra("lecturerId", appointment.lecturerId)
//            putExtra("oldDate", appointment.date)
//            putExtra("oldTime", appointment.time)
//        }
//        startActivity(intent)
//        Toast.makeText(requireContext(), "Review ${appointment.module}", Toast.LENGTH_SHORT).show()
//        reviewButton.setOnClickListener {
        val intent = Intent(context, lecturer_review::class.java).apply {
            putExtra("appointmentId", appointment.id)
            putExtra("studentId", userId)
            putExtra("lecturerId", appointment.lecturerId)
//
        }
        startActivity(intent)
        //   context.startActivity(intent)
//        }
    }
}
