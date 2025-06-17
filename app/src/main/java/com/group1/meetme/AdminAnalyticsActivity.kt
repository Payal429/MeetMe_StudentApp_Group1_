package com.group1.meetme

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.group1.meetme.databinding.ActivityAdminAnalyticsBinding
import java.util.Calendar
import java.util.Locale

class AdminAnalyticsActivity : AppCompatActivity() {

    // View binding for the activity layout
    private lateinit var binding: ActivityAdminAnalyticsBinding

    // Optional date range for filtering appointment data
    private var startDate: String? = null
    private var endDate: String? = null

    // Firebase database references
    private val db = FirebaseDatabase.getInstance().reference
    private val usersRef = db.child("users")
    private val appointmentsRef = db.child("appointments")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding for layout
        binding = ActivityAdminAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // When user clicks "Start Date", open date picker and update text
        binding.startDateBtn.setOnClickListener {
            pickDate {
                startDate = it; binding.startDateBtn.text = it
            }
        }

        // When user clicks "End Date", open date picker and update text
        binding.endDateBtn.setOnClickListener {
            pickDate {
                endDate = it; binding.endDateBtn.text = it
            }
        }

        // Apply filters and reload stats when the "Apply Filter" button is clicked
        binding.applyFilterBtn.setOnClickListener { loadStats() }

        // Load unfiltered stats on launch
        loadStats()

        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        // Find the back arrow button and set an OnClickListener to navigate back to the dashboard.
        backArrow.setOnClickListener() {
            finish()
        }
    }

    // Function to open a DatePickerDialog and return the selected date as a string
    private fun pickDate(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
                onDateSelected(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Load and compute statistics from Firebase based on optional date filter
    private fun loadStats() {
        val studentsByFaculty = mutableMapOf<String, Int>()
        val lecturersByFaculty = mutableMapOf<String, Int>()
        val appointmentsByStatus = mutableMapOf<String, Int>()
        val appointmentsByFaculty = mutableMapOf<String, Int>()

        // Load all students and group them by course/faculty
        usersRef.child("Student").get().addOnSuccessListener { studentSnap ->
            for (snap in studentSnap.children) {
                val course = snap.child("course").value?.toString() ?: continue
                studentsByFaculty[course] = (studentsByFaculty[course] ?: 0) + 1
            }

            // Load all lecturers and group them by course/faculty
            usersRef.child("Lecturer").get().addOnSuccessListener { lecturerSnap ->
                val lecturerCourses = mutableMapOf<String, String>() // lecturerId -> course
                for (snap in lecturerSnap.children) {
                    val id = snap.key ?: continue
                    val course = snap.child("course").value?.toString() ?: continue
                    lecturersByFaculty[course] = (lecturersByFaculty[course] ?: 0) + 1
                    lecturerCourses[id] = course
                }

                // Load all appointments and group by status and faculty
                appointmentsRef.get().addOnSuccessListener { apptSnap ->
                    for (studentAppts in apptSnap.children) {
                        for (appt in studentAppts.children) {
                            val date = appt.child("date").value?.toString()
                            val status = appt.child("status").value?.toString() ?: "unknown"
                            val lecturerId = appt.child("lecturerId").value?.toString() ?: continue

                            if (isWithinDateRange(date)) {
                                appointmentsByStatus[status] =
                                    (appointmentsByStatus[status] ?: 0) + 1
                                val faculty = lecturerCourses[lecturerId] ?: "Unknown"
                                appointmentsByFaculty[faculty] =
                                    (appointmentsByFaculty[faculty] ?: 0) + 1
                            }
                        }
                    }

                    // After loading everything, display the stats
                    showStats(
                        studentsByFaculty,
                        lecturersByFaculty,
                        appointmentsByStatus,
                        appointmentsByFaculty
                    )
                }
            }
        }
        // Load saved language preference
        loadLanguage()
    }

    // Load saved language preference and apply it to this activity
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Set the app's locale to the selected language
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Check if a given date is within the selected date range
    private fun isWithinDateRange(date: String?): Boolean {
        if (date.isNullOrEmpty()) return false
        if (startDate == null || endDate == null) return true // No filter

        return date >= startDate!! && date <= endDate!!
    }

    // Display statistics in a formatted text view
    private fun showStats(
        students: Map<String, Int>,
        lecturers: Map<String, Int>,
        apptStatus: Map<String, Int>,
        apptFaculty: Map<String, Int>
    ) {
        val builder = StringBuilder()

        // Append student count per faculty
        builder.append("📊 Student Count by Faculty:\n")
        for ((faculty, count) in students) builder.append("• $faculty: $count\n")

        // Append lecturer  count per faculty
        builder.append("\n👩‍🏫 Lecturer Count by Faculty:\n")
        for ((faculty, count) in lecturers) builder.append("• $faculty: $count\n")

        // Append appointments grouped by status
        builder.append("\n📅 Appointments by Status:\n")
        for ((status, count) in apptStatus) builder.append("• ${status.capitalize()}: $count\n")

        // Append appointments grouped by faculty
        builder.append("\n🏫 Appointments by Faculty:\n")
        for ((faculty, count) in apptFaculty) builder.append("• $faculty: $count\n")

        // Show results in the TextView
        binding.statsTextView.text = builder.toString()
    }
}
