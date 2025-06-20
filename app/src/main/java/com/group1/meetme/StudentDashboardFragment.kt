package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale

class StudentDashboardFragment : Fragment() {

    // RecyclerView to display upcoming appointments
    private lateinit var recyclerView: RecyclerView
    // Adapter for RecyclerView to bind appointment data
    private lateinit var adapter: UpcomingAppointmentsAdapter
    // List to hold upcoming appointment data
    private val upcomingAppointments = mutableListOf<Appointment>()

    // Buttons for navigation on the dashboard
    private lateinit var scheduleButton: ImageButton
    private lateinit var bookingsButton: ImageButton
    private lateinit var resourceButton: ImageButton
    private lateinit var settingsButton: ImageButton

    // Inflate the fragment layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_student_dashboard, container, false)
    }

    // Called after the fragment's view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        scheduleButton = view.findViewById(R.id.schedulebutton)
        bookingsButton = view.findViewById(R.id.bookingsbutton)
        settingsButton = view.findViewById(R.id.settingsbutton)

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Retrieve user ID and role from SharedPreferences for data filtering
        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        // Create adapter passing the appointment list and user role
        adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        recyclerView.adapter = adapter

        // Load upcoming appointments from Firebase for the current user
        loadUpcomingAppointments(userId, userType)

        // Navigation listeners
        scheduleButton.setOnClickListener {
            findNavController().navigate(R.id.studentBookingFragment)
        }

        bookingsButton.setOnClickListener {
            findNavController().navigate(R.id.studentBookingsListFragment)
        }

        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.SettingsFragment)
        }

        // Language support
        loadLanguage()


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val alertDialog = AlertDialog.Builder(requireContext()).create()
                alertDialog.setTitle("Logout")
                alertDialog.setMessage("Are you sure you want to Logout?")

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->
                    val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("ID_NUM", "")
                    editor.apply()

                    // Finish the activity (log out and close app)
                    requireActivity().finishAffinity()
                    dialog.dismiss()
                }

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
                    dialog.dismiss()
                }

                alertDialog.show()
            }
        })

    }

    // Load saved language preference from SharedPreferences and set locale
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Apply the specified locale/language for the app resources
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Fetch upcoming appointments from Firebase under the current user's node
    private fun loadUpcomingAppointments(userId: String, userType: String) {
        // Get a reference to the Firebase Realtime Database
        val db = FirebaseDatabase.getInstance().reference

        // Navigate to the appointments node under the user ID
        db.child("appointments").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                // This callback is triggered once with the data from the database
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear the current list to avoid duplicates
                    upcomingAppointments.clear()

                    // Loop through each child (individual appointment)
                    for (snap in snapshot.children) {
                        // Convert snapshot to an Appointment object
                        val appointment = snap.getValue(Appointment::class.java)

                        // Only include appointments with status = "upcoming"
                        if (appointment?.status == "upcoming") {
                            // Assign the Firebase key as the ID of the appointment
                            appointment.id = snap.key!!
                            // Add to the upcomingAppointments list
                            upcomingAppointments.add(appointment)
                        }
                    }
                    // Notify the adapter to refresh the UI (RecyclerView/ListView)
                    adapter.notifyDataSetChanged()
                }

                // Handle error case when Firebase read is cancelled or fails
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Helper function to get stored user ID from SharedPreferences
    private fun getUserIdFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    // Helper function to get stored user role from SharedPreferences
    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }

}