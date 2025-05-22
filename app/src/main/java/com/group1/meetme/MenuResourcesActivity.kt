package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MenuResourcesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_resources)



        // Reference to the Upload Resources card
        val uploadLayout = findViewById<LinearLayout>(R.id.uploadResourcesLayout)

        // Reference to the Download Resources card
        val downloadLayout = findViewById<LinearLayout>(R.id.downloadResourcesLayout)

        // Open UploadResourcesActivity when Upload card is tapped
        uploadLayout.setOnClickListener {
            val intent = Intent(this, UploadResourcesActivity::class.java)
            startActivity(intent)
        }

        // Open DownloadResourcesActivity when Download card is tapped
        downloadLayout.setOnClickListener {
            val intent = Intent(this, DownloadResourcesActivity::class.java)
            startActivity(intent)
        }
//        val homebutton: ImageButton = findViewById(R.id.homebutton)
//        val schedulebutton: ImageButton = findViewById(R.id.schedulebutton)
//        val bookingsbutton: ImageButton = findViewById(R.id.bookingsbutton)
//        val settingsbutton: ImageButton = findViewById(R.id.settingsbutton)
//        val resourcebutton: ImageButton = findViewById(R.id.resourcesbutton)

        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener(){
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

//        homebutton.setOnClickListener {
//            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
//            val role = sharedPref.getString("role", "student") // default to student
//
//            val intent = if (role == "lecturer") {
//                Intent(this, LecturerDashboardActivity::class.java)
//            } else {
//                Intent(this, StudentDashboardActivity::class.java)
//            }
//
//            startActivity(intent)
//        }
//
//        schedulebutton.setOnClickListener(){
//            val intent = Intent(this, BookAppointmentActivity::class.java)
//            startActivity(intent)
//        }
//
//        bookingsbutton.setOnClickListener(){
//            val intent = Intent(this, AppointmentsActivity::class.java)
//            startActivity(intent)
//        }
//
//        resourcebutton.setOnClickListener(){
//            val intent = Intent(this, MenuResourcesActivity::class.java)
//            startActivity(intent)
//        }
//
//
//        settingsbutton.setOnClickListener(){
//
//        }

    }
}