package com.group1.meetme

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class AdminAnalyticsActivity : AppCompatActivity() {

    private lateinit var startDateBtn: Button
    private lateinit var endDateBtn: Button
    private lateinit var applyFilterBtn: Button
    private lateinit var statsTextView: TextView

    private var startDate: String? = null
    private var endDate: String? = null

    private val db = FirebaseDatabase.getInstance().reference
    private val usersRef = db.child("users")
    private val appointmentsRef = db.child("appointments")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_analytics)

        startDateBtn = findViewById(R.id.startDateBtn)
        endDateBtn = findViewById(R.id.endDateBtn)
        applyFilterBtn = findViewById(R.id.applyFilterBtn)
        statsTextView = findViewById(R.id.statsTextView)

        startDateBtn.setOnClickListener { pickDate { startDate = it; startDateBtn.text = it } }
        endDateBtn.setOnClickListener { pickDate { endDate = it; endDateBtn.text = it } }

        applyFilterBtn.setOnClickListener { loadStats() }

        loadStats() // Load unfiltered stats on launch
    }

    private fun pickDate(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            onDateSelected(formatted)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadStats() {
        val studentsByFaculty = mutableMapOf<String, Int>()
        val lecturersByFaculty = mutableMapOf<String, Int>()
        val appointmentsByStatus = mutableMapOf<String, Int>()
        val appointmentsByFaculty = mutableMapOf<String, Int>()

        usersRef.child("Student").get().addOnSuccessListener { studentSnap ->
            for (snap in studentSnap.children) {
                val course = snap.child("course").value?.toString() ?: continue
                studentsByFaculty[course] = (studentsByFaculty[course] ?: 0) + 1
            }

            usersRef.child("Lecturer").get().addOnSuccessListener { lecturerSnap ->
                val lecturerCourses = mutableMapOf<String, String>() // lecturerId -> course
                for (snap in lecturerSnap.children) {
                    val id = snap.key ?: continue
                    val course = snap.child("course").value?.toString() ?: continue
                    lecturersByFaculty[course] = (lecturersByFaculty[course] ?: 0) + 1
                    lecturerCourses[id] = course
                }

                appointmentsRef.get().addOnSuccessListener { apptSnap ->
                    for (studentAppts in apptSnap.children) {
                        for (appt in studentAppts.children) {
                            val date = appt.child("date").value?.toString()
                            val status = appt.child("status").value?.toString() ?: "unknown"
                            val lecturerId = appt.child("lecturerId").value?.toString() ?: continue

                            if (isWithinDateRange(date)) {
                                appointmentsByStatus[status] = (appointmentsByStatus[status] ?: 0) + 1
                                val faculty = lecturerCourses[lecturerId] ?: "Unknown"
                                appointmentsByFaculty[faculty] = (appointmentsByFaculty[faculty] ?: 0) + 1
                            }
                        }
                    }

                    showStats(studentsByFaculty, lecturersByFaculty, appointmentsByStatus, appointmentsByFaculty)
                }
            }
        }
    }

    private fun isWithinDateRange(date: String?): Boolean {
        if (date.isNullOrEmpty()) return false
        if (startDate == null || endDate == null) return true // No filter

        return date >= startDate!! && date <= endDate!!
    }

    private fun showStats(
        students: Map<String, Int>,
        lecturers: Map<String, Int>,
        apptStatus: Map<String, Int>,
        apptFaculty: Map<String, Int>
    ) {
        val builder = StringBuilder()

        builder.append("📊 Student Count by Faculty:\n")
        for ((faculty, count) in students) builder.append("• $faculty: $count\n")

        builder.append("\n👩‍🏫 Lecturer Count by Faculty:\n")
        for ((faculty, count) in lecturers) builder.append("• $faculty: $count\n")

        builder.append("\n📅 Appointments by Status:\n")
        for ((status, count) in apptStatus) builder.append("• ${status.capitalize()}: $count\n")

        builder.append("\n🏫 Appointments by Faculty:\n")
        for ((faculty, count) in apptFaculty) builder.append("• $faculty: $count\n")

        statsTextView.text = builder.toString()
    }
}
