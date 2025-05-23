package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

// Activity for the student's dashboard.
class StudentDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the login button by its ID
        val homebutton: ImageButton = findViewById(R.id.homebutton)
        val schedulebutton: ImageButton = findViewById(R.id.schedulebutton)
        val bookingsbutton: ImageButton = findViewById(R.id.bookingsbutton)
        //  val downloadResource: ImageButton = findViewById(R.id.downloadResources)
        val resourcebutton: ImageButton = findViewById(R.id.resourcesbutton)
        val settingsbutton: ImageButton = findViewById(R.id.settingsbutton)

        // Set up the schedule button click listener.
        schedulebutton.setOnClickListener(){
            val intent = Intent(this, BookAppointmentActivity::class.java)
            startActivity(intent)
        }

        // Set up the bookings button click listener
        bookingsbutton.setOnClickListener(){
            val intent = Intent(this, AppointmentsActivity::class.java)
            startActivity(intent)
        }
        // Set up the resources button click listener.
        resourcebutton.setOnClickListener(){
            val intent = Intent(this, DownloadResourcesActivity::class.java)
            startActivity(intent)
        }
        // Set up the settings button click listener.
        settingsbutton.setOnClickListener(){
            val intent = Intent(this,SettingsActivity::class.java)
            startActivity(intent)
        }
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
            dialog.dismiss()
        }

        // Set up the negative button (No) to dismiss the dialog.
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which ->
            dialog.dismiss()
        }
        // Show the AlertDialog.
        alertDialog.show()
    }
}