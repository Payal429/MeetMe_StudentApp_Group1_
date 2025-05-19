package com.group1.meetme

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.group1.meetme.Appointment
import com.group1.meetme.AppointmentAdapter

class CancelledAppointmentsFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AppointmentAdapter
    private val appointments = mutableListOf<Appointment>()

    private var userId: String? = null
    private var userType: String? = null

    companion object {
        fun newInstance(userId: String, userType: String): CancelledAppointmentsFragment {
            val fragment = CancelledAppointmentsFragment()
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
        val view = inflater.inflate(R.layout.fragment_cancelled_appointments, container, false)
        listView = view.findViewById(R.id.appointmentsListView)
        database = FirebaseDatabase.getInstance().reference

//        // Get user ID and role from SharedPreferences
//        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
//        userId = sharedPreferences.getString("ID_NUM", null)
//        userType = sharedPreferences.getString("USER_TYPE", null)

        super.onCreate(savedInstanceState)
        userId = arguments?.getString("USER_ID")
        userType = arguments?.getString("USER_ROLE")
        Log.d("FragmentArgs", "userId = $userId, userType = $userType")

        if (userId == null || userType == null) {
            Toast.makeText(requireContext(), "User info not found", Toast.LENGTH_SHORT).show()
            return view
        }

        adapter = AppointmentAdapter(
            requireContext(),
            appointments,
            userType = userType!!,
            onAction1 = { /* No action for cancelled */ },
            onAction2 = { /* No action for cancelled */ }
        )
        listView.adapter = adapter

        loadCancelledAppointments()

        return view
    }

    private fun loadCancelledAppointments() {
        database.child("appointments").child(userId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    appointments.clear()

                    for (snap in snapshot.children) {
                        val appointment = snap.getValue(Appointment::class.java)
                        val key = snap.key ?: continue
                        appointment?.id = key

                        if (appointment != null && appointment.status == "cancelled") {
                            appointments.add(appointment)
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CancelledAppointments", "Error loading appointments: ${error.message}")
                }
            })
    }
}
