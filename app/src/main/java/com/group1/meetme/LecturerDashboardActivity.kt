package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Locale

// Activity for the lecturer's dashboard.
class LecturerDashboardActivity : AppCompatActivity() {

    // RecyclerViews for displaying upcoming and previous appointments
    private lateinit var recyclerViewUpcoming: RecyclerView
    private lateinit var recyclerViewPrevious: RecyclerView

    // Adapters for the RecyclerViews
    private lateinit var adapter: UpcomingAppointmentsAdapter
    private lateinit var adapterPrevious: UpcomingAppointmentsAdapter

    // Lists to hold the appointments
    private val upcomingAppointments = mutableListOf<Appointment>()
    private val previousAppointments = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Makes the app full screen edge-to-edge
        enableEdgeToEdge()

        setContentView(R.layout.activity_lecturer_dashboard)

        // Adjust padding to avoid UI overlap with system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the login button by its ID
        val homebutton: ImageButton = findViewById(R.id.homebutton)
        val schedulebutton: ImageButton = findViewById(R.id.schedulebutton)
        val bookingsbutton: ImageButton = findViewById(R.id.bookingsbutton)
        val uploadResources: ImageButton = findViewById(R.id.uploadResources)
        val settingsbutton: ImageButton = findViewById(R.id.settingsbutton)


        // Enable offline persistence for Firebase Database.
        Firebase.database.setPersistenceEnabled(true)

        // RecyclerView for the upcoming appointments.
        recyclerViewUpcoming = findViewById(R.id.appointmentsRecyclerViewUpcoming)
        recyclerViewUpcoming.layoutManager = LinearLayoutManager(this)

        recyclerViewPrevious = findViewById(R.id.appointmentsRecyclerViewPrevious)
        recyclerViewPrevious.layoutManager = LinearLayoutManager(this)

        // Get logged-in user's ID and type from shared preferences
        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        // Set up adapters and attach them to the RecyclerViews
        adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        adapterPrevious = UpcomingAppointmentsAdapter(previousAppointments, userType)
        recyclerViewUpcoming.adapter = adapter
        recyclerViewPrevious.adapter = adapterPrevious

        // Load appointments from Firebase
        loadUpcomingAppointments(userId, userType)
        loadPreviousAppointments(userId, userType)

        // Set up the schedule button click listener.
        schedulebutton.setOnClickListener() {
            val intent = Intent(this, ScheduleAvailabilityActivity::class.java)
            startActivity(intent)
        }

        // Set up the bookings button click listener.
        bookingsbutton.setOnClickListener() {
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }
        // Set up the upload resources button click listener.
        uploadResources.setOnClickListener() {
            val intent = Intent(this, UploadResourcesActivity::class.java)
            startActivity(intent)
        }

        // Set up the settings button click listener.
        settingsbutton.setOnClickListener() {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        // Load saved language preference
        loadLanguage()
    }

    // Loads saved language preference and applies it
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Changes the app's locale based on the selected language
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Override the back button to show a logout confirmation dialog.
    override fun onBackPressed() {
        // Create an AlertDialog to confirm logout.
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to Logout?")

        // Set up the positive button (Yes) to clear shared preferences and log out.
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, which ->
            // Clear the user ID from shared preferences.
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("ID_NUM", "")
            editor.apply()

            // Call the superclass onBackPressed to finish the activity.
            super.onBackPressed()
            // Finish all activities and exit the app gracefully
            finishAffinity()  // closes all activities
            dialog.dismiss()
        }

        // Set up the negative button (No) to dismiss the dialog.
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    // Loads upcoming appointments from Firebase
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
                    Toast.makeText(
                        this@LecturerDashboardActivity,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Loads completed (previous) appointments from Firebase
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
                    Toast.makeText(
                        this@LecturerDashboardActivity,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Fetches the user ID from shared preferences
    private fun getUserIdFromSharedPreferences(): String {
        val prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    // Fetches the user type (role) from shared preferences
    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }
}