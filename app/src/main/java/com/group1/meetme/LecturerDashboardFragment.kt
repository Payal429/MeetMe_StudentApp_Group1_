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

    // RecyclerViews for displaying upcoming and previous appointments
    private lateinit var recyclerViewUpcoming: RecyclerView
    private lateinit var recyclerViewPrevious: RecyclerView

    // Adapters for the RecyclerViews
    private lateinit var adapter: UpcomingAppointmentsAdapter
    private lateinit var adapterPrevious: UpcomingAppointmentsAdapter

    // Lists to hold the appointments
    private val upcomingAppointments = mutableListOf<Appointment>()
    private val previousAppointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout XML for this fragment
        val view = inflater.inflate(R.layout.fragment_lecturer_dashboard, container, false)

        // Initialize RecyclerView for upcoming appointments and set its layout manager
        recyclerViewUpcoming = view.findViewById(R.id.appointmentsRecyclerViewUpcoming)
        recyclerViewUpcoming.layoutManager = LinearLayoutManager(requireContext())

        // Initialize RecyclerView for previous (completed) appointments and set its layout manager
        recyclerViewPrevious = view.findViewById(R.id.appointmentsRecyclerViewPrevious)
        recyclerViewPrevious.layoutManager = LinearLayoutManager(requireContext())

        // Initialize navigation buttons from the layout
        val homebutton: ImageButton = view.findViewById(R.id.homebutton)
        val schedulebutton: ImageButton = view.findViewById(R.id.schedulebutton)
        val bookingsbutton: ImageButton = view.findViewById(R.id.bookingsbutton)
        val settingsbutton: ImageButton = view.findViewById(R.id.settingsbutton)

        // Retrieve the current user ID and user role from shared preferences
        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        // Create adapters, passing the appointment lists and user type
        adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        adapterPrevious = UpcomingAppointmentsAdapter(previousAppointments, userType)

        // Attach the adapters to their respective RecyclerViews
        recyclerViewUpcoming.adapter = adapter
        recyclerViewPrevious.adapter = adapterPrevious

        // Load appointment data from Firebase for both upcoming and previous appointments
        loadUpcomingAppointments(userId, userType)
        loadPreviousAppointments(userId, userType)

        // Set click listener for schedule button to navigate to ScheduleAvailabilityFragment
        schedulebutton.setOnClickListener {
            findNavController().navigate(R.id.ScheduleAvailabilityFragment)
        }

        // Set click listener for bookings button to navigate to LecturerBookingsListFragment
        bookingsbutton.setOnClickListener {
            findNavController().navigate(R.id.LecturerBookingsListFragment)
        }

        // Set click listener for settings button to navigate to SettingsFragment
        settingsbutton.setOnClickListener {
            findNavController().navigate(R.id.SettingsFragment)
        }

        // Load the saved language preference and apply it to the app locale
        loadLanguage()
        // Return the root view of the fragment
        return view
    }

    // Loads the saved language preference from shared preferences and applies it
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Sets the app locale based on the language code provided
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Loads upcoming appointments for the lecturer from Firebase Realtime Database
    private fun loadUpcomingAppointments(userId: String, userType: String) {
        val db = FirebaseDatabase.getInstance().reference
        // Access the appointmentsLecturer node for the current userId
        db.child("appointmentsLecturer").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear existing data to avoid duplicates
                    upcomingAppointments.clear()
                    for (snap in snapshot.children) {
                        // Parse each appointment snapshot into Appointment object
                        val appointment = snap.getValue(Appointment::class.java)
                        // Filter for only appointments with status "upcoming"
                        if (appointment?.status == "upcoming") {
                            // Assign the Firebase key as the appointment id for reference
                            appointment.id = snap.key!!
                            upcomingAppointments.add(appointment)
                        }
                    }
                    // Notify the adapter that the data has changed so the list refreshes
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Show a toast message if there was an error retrieving data
                    Toast.makeText(
                        requireContext(),
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Loads previous (completed) appointments for the lecturer from Firebase Realtime Database
    private fun loadPreviousAppointments(userId: String, userType: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("appointmentsLecturer").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    previousAppointments.clear()
                    for (snap in snapshot.children) {
                        val appointment = snap.getValue(Appointment::class.java)
                        // Filter for appointments with status "completed"
                        if (appointment?.status == "completed") {
                            appointment.id = snap.key!!
                            previousAppointments.add(appointment)
                        }
                    }
                    adapterPrevious.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Retrieves the currently logged-in user's ID from shared preferences
    private fun getUserIdFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    // Retrieves the currently logged-in user's role/type from shared preferences
    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }
}