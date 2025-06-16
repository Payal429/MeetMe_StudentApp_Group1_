package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale


class LecturerDashboardFragment : Fragment() {
        private lateinit var recyclerViewUpcoming: RecyclerView
        private lateinit var recyclerViewPrevious: RecyclerView
        private lateinit var adapter: UpcomingAppointmentsAdapter
        private lateinit var adapterPrevious: UpcomingAppointmentsAdapter
        private val upcomingAppointments = mutableListOf<Appointment>()
        private val previousAppointments = mutableListOf<Appointment>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_lecturer_dashboard, container, false)

            recyclerViewUpcoming = view.findViewById(R.id.appointmentsRecyclerViewUpcoming)
            recyclerViewUpcoming.layoutManager = LinearLayoutManager(requireContext())

            recyclerViewPrevious = view.findViewById(R.id.appointmentsRecyclerViewPrevious)
            recyclerViewPrevious.layoutManager = LinearLayoutManager(requireContext())

            val homebutton: ImageButton = view.findViewById(R.id.homebutton)
            val schedulebutton: ImageButton = view.findViewById(R.id.schedulebutton)
            val bookingsbutton: ImageButton = view.findViewById(R.id.bookingsbutton)
//            val uploadResources: ImageButton = view.findViewById(R.id.uploadResources)
            val settingsbutton: ImageButton = view.findViewById(R.id.settingsbutton)

            val userId = getUserIdFromSharedPreferences()
            val userType = getUserTypeFromSharedPreferences()

            adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
            adapterPrevious = UpcomingAppointmentsAdapter(previousAppointments, userType)
            recyclerViewUpcoming.adapter = adapter
            recyclerViewPrevious.adapter = adapterPrevious

            loadUpcomingAppointments(userId, userType)
            loadPreviousAppointments(userId, userType)

            schedulebutton.setOnClickListener {
                findNavController().navigate(R.id.ScheduleAvailabilityFragment)
            }

            bookingsbutton.setOnClickListener {
                findNavController().navigate(R.id.LecturerBookingsListFragment)
            }

//            uploadResources.setOnClickListener {
//                findNavController().navigate(R.id.action_LecturerDashboard_to_UploadResources)
//            }

            settingsbutton.setOnClickListener {
                findNavController().navigate(R.id.SettingsFragment)
            }

            loadLanguage()

            return view
        }

        private fun loadLanguage() {
            val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            val savedLanguage = sharedPref.getString("language", "en")
            setLocale(savedLanguage ?: "en")
        }

        private fun setLocale(languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        private fun loadUpcomingAppointments(userId: String, userType: String) {
            val db = FirebaseDatabase.getInstance().reference
            db.child("appointmentsLecturer").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        upcomingAppointments.clear()
                        for (snap in snapshot.children) {
                            val appointment = snap.getValue(Appointment::class.java)
                            if (appointment?.status == "upcoming") {
                                appointment.id = snap.key!!
                                upcomingAppointments.add(appointment)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun loadPreviousAppointments(userId: String, userType: String) {
            val db = FirebaseDatabase.getInstance().reference
            db.child("appointmentsLecturer").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        previousAppointments.clear()
                        for (snap in snapshot.children) {
                            val appointment = snap.getValue(Appointment::class.java)
                            if (appointment?.status == "completed") {
                                appointment.id = snap.key!!
                                previousAppointments.add(appointment)
                            }
                        }
                        adapterPrevious.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun getUserIdFromSharedPreferences(): String {
            val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            return prefs.getString("ID_NUM", "") ?: ""
        }

        private fun getUserTypeFromSharedPreferences(): String {
            val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            return prefs.getString("USER_ROLE", "") ?: ""
        }
    }
