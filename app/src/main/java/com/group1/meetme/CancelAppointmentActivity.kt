package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group1.meetme.databinding.ActivityCancelAppointmentBinding
import java.util.Locale

// Activity for canceling appointments.
class CancelAppointmentActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivityCancelAppointmentBinding

    // UI components.
//    private lateinit var reasonGroup: RadioGroup
//    private lateinit var otherReasonInput: EditText
//    private lateinit var confirmButton: Button

    // Data passed from the previous activity.
    private lateinit var appointmentId: String
    private lateinit var studentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_cancel_appointment)

        binding = ActivityCancelAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components.
//        reasonGroup = findViewById(R.id.reasonGroup)
//        otherReasonInput = findViewById(R.id.otherReasonInput)
//        confirmButton = findViewById(R.id.confirmCancelButton)

        // Retrieve the appointment ID and student ID from the intent.
        appointmentId = intent.getStringExtra("appointmentId")!!
        studentId = intent.getStringExtra("studentId")!!

        // Set up the confirm button to handle appointment cancellation.
        binding.confirmCancelButton.setOnClickListener {
            // Get the selected reason for cancellation.
            val selectedId = binding.reasonGroup.checkedRadioButtonId
            // If "Other" is selected, use the input from the EditText.
            val reason = if (selectedId == R.id.reasonOther) {
                binding.otherReasonInput.text.toString().ifEmpty { "No reason provided" }
            } else {
                // Otherwise, use the text from the selected RadioButton.
                findViewById<RadioButton>(selectedId).text.toString()
            }
            // Call the function to cancel the appointment.
            cancelAppointment(reason)
        }
        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener() {
//            val intent = Intent(this, StudentDashboardActivity::class.java)
//            startActivity(intent)
            finish()
        }
        // Load saved language preference
        loadLanguage()

    }

    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

//    private fun cancelAppointment(reason: String) {
//        val db = FirebaseDatabase.getInstance().reference
//        db.child("appointments").child(studentId).child(appointmentId)
//            .updateChildren(mapOf(
//                "status" to "cancelled",
//                "cancellationReason" to reason
//            ))
//            .addOnSuccessListener {
//                Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show()
//            }
//    }

//    // Function to cancel an appointment.
//    private fun cancelAppointment(reason: String) {
//        val db = FirebaseDatabase.getInstance().reference
//
//        // Step 1: Get the full appointment data for the student
//        db.child("appointments").child(studentId).child(appointmentId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val appointment = snapshot.getValue(Appointment::class.java)
//                    if (appointment != null) {
//                        val lecturerId = appointment.lecturerId ?: return
//                        val date = appointment.date
//                        val time = appointment.time
//
//                        // Step 2: Update the student's appointment
//                        val updates = mapOf(
//                            "status" to "cancelled",
//                            "cancellationReason" to reason
//                        )
//                        db.child("appointments").child(studentId).child(appointmentId)
//                            .updateChildren(updates)
//
//                        // Step 3: Find the matching appointment under appointmentsLecturer
//                        db.child("appointmentsLecturer").child(lecturerId)
//                            .addListenerForSingleValueEvent(object : ValueEventListener {
//                                override fun onDataChange(lecturerSnapshot: DataSnapshot) {
//                                    for (lecturerAppSnap in lecturerSnapshot.children) {
//                                        val lecApp =
//                                            lecturerAppSnap.getValue(AppointmentLecturer::class.java)
//                                        val lecKey = lecturerAppSnap.key ?: continue
//
//                                        // Match by date, time, and studentID
//                                        if (lecApp?.date == date &&
//                                            lecApp.time == time &&
//                                            lecApp.studentId == studentId
//                                        ) {
//                                            db.child("appointmentsLecturer")
//                                                .child(lecturerId)
//                                                .child(lecKey)
//                                                .updateChildren(updates)
//                                            break
//                                        }
//                                    }
//
//                                    // Show a success message and finish the activity.
//                                    Toast.makeText(
//                                        this@CancelAppointmentActivity,
//                                        "Appointment cancelled",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    finish()
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                    Toast.makeText(
//                                        // Show an error message if the operation is cancelled.
//                                        this@CancelAppointmentActivity,
//                                        "Error updating lecturer view",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            })
//                    } else {
//                        // Show an error message if the appointment is not found.
//                        Toast.makeText(
//                            this@CancelAppointmentActivity,
//                            "Appointment not found",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                // Show an error message if the operation is cancelled.
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(
//                        this@CancelAppointmentActivity,
//                        "Error accessing appointment",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            })
//    }

    private fun cancelAppointment(reason: String) {
        val db = FirebaseDatabase.getInstance().reference

        // Step 1: Get the full appointment data for the student
        db.child("appointments").child(studentId).child(appointmentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointment = snapshot.getValue(Appointment::class.java)
                    if (appointment != null) {
                        val lecturerId = appointment.lecturerId ?: return

                        // Step 2: Create update map
                        val updates = mapOf(
                            "status" to "cancelled",
                            "cancellationReason" to reason
                        )

                        Log.d("Cancel Student ID", studentId)
                        Log.d("Cancel Lecturer ID", lecturerId)
                        Log.d("Cancel Appointment ID", appointmentId)


                        // Step 3: Update student appointment
                        db.child("appointments").child(studentId).child(appointmentId)
                            .updateChildren(updates)

                        // Step 4: Update lecturer appointment using same ID
                        db.child("appointmentsLecturer").child(lecturerId).child(appointmentId)
                            .updateChildren(updates)

                        // Success message
                        Toast.makeText(
                            this@CancelAppointmentActivity,
                            "Appointment cancelled",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@CancelAppointmentActivity,
                            "Appointment not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@CancelAppointmentActivity,
                        "Error accessing appointment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}
