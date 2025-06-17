package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

// Activity for the student's dashboard.
class StudentDashboardActivity : AppCompatActivity() {

    // RecyclerView to display upcoming appointments
    private lateinit var recyclerView: RecyclerView
    // Adapter for RecyclerView to bind appointment data
    private lateinit var adapter: UpcomingAppointmentsAdapter
    // List to hold upcoming appointment data
    private val upcomingAppointments = mutableListOf<Appointment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Enable offline persistence for Firebase Database.
        // Firebase.database.setPersistenceEnabled(true)

        // Find the login button by its ID
        val homebutton: ImageButton = findViewById(R.id.homebutton)
        val schedulebutton: ImageButton = findViewById(R.id.schedulebutton)
        val bookingsbutton: ImageButton = findViewById(R.id.bookingsbutton)
        val resourcebutton: ImageButton = findViewById(R.id.resourcesbutton)
        val settingsbutton: ImageButton = findViewById(R.id.settingsbutton)


        // RecyclerView for the upcoming appointments.
        recyclerView = findViewById(R.id.appointmentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve user ID and role from SharedPreferences for data filtering
        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        // Create adapter passing the appointment list and user role
        adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        recyclerView.adapter = adapter

        // Load upcoming appointments from Firebase for the current user
        loadUpcomingAppointments(userId, userType)

        // Set up the schedule button click listener.
        schedulebutton.setOnClickListener() {
            val intent = Intent(this, BookAppointmentActivity::class.java)
            startActivity(intent)
        }

        // Set up the bookings button click listener
        bookingsbutton.setOnClickListener() {
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }
        // Set up the resources button click listener.
        resourcebutton.setOnClickListener() {
            val intent = Intent(this, DownloadResourcesActivity::class.java)
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

    // Load saved language preference from SharedPreferences and set locale
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
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

    // Override the back button to show a logout confirmation dialog.
    override fun onBackPressed() {
        // Create an AlertDialog to confirm logout.
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to Logout?")

        // Set up the positive button (Yes) to clear shared preferences and log out.
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, which ->
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("ID_NUM", "")
            editor.apply()

            // Call the superclass onBackPressed to finish the activity
            super.onBackPressed()
            // Finish all activities and exit the app gracefully
            finishAffinity()
            dialog.dismiss()
        }

        // Set up the negative button (No) to dismiss the dialog.
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which ->
            dialog.dismiss()
        }
        // Show the AlertDialog.
        alertDialog.show()
    }

    // Fetch upcoming appointments from Firebase under the current user's node
    private fun loadUpcomingAppointments(userId: String, userType: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("appointments").child(userId)
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
                        this@StudentDashboardActivity,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Helper function to get stored user ID from SharedPreferences
    private fun getUserIdFromSharedPreferences(): String {
        val prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    // Helper function to get stored user role from SharedPreferences
    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }
}