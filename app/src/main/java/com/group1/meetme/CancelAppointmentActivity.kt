package com.group1.meetme

import android.content.Intent
import android.os.Bundle
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

class CancelAppointmentActivity : AppCompatActivity() {

    private lateinit var reasonGroup: RadioGroup
    private lateinit var otherReasonInput: EditText
    private lateinit var confirmButton: Button

    private lateinit var appointmentId: String
    private lateinit var studentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_appointment)

        reasonGroup = findViewById(R.id.reasonGroup)
        otherReasonInput = findViewById(R.id.otherReasonInput)
        confirmButton = findViewById(R.id.confirmCancelButton)

        appointmentId = intent.getStringExtra("appointmentId")!!
        studentId = intent.getStringExtra("studentId")!!

        confirmButton.setOnClickListener {
            val selectedId = reasonGroup.checkedRadioButtonId
            val reason = if (selectedId == R.id.reasonOther) {
                otherReasonInput.text.toString().ifEmpty { "No reason provided" }
            } else {
                findViewById<RadioButton>(selectedId).text.toString()
            }
            cancelAppointment(reason)
        }
        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener(){
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }
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

    private fun cancelAppointment(reason: String) {
        val db = FirebaseDatabase.getInstance().reference

        // Step 1: Get the full appointment data for the student
        db.child("appointments").child(studentId).child(appointmentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val appointment = snapshot.getValue(Appointment::class.java)
                    if (appointment != null) {
                        val lecturerId = appointment.lecturerId ?: return
                        val date = appointment.date
                        val time = appointment.time

                        // Step 2: Update the student's appointment
                        val updates = mapOf(
                            "status" to "cancelled",
                            "cancellationReason" to reason
                        )
                        db.child("appointments").child(studentId).child(appointmentId).updateChildren(updates)

                        // Step 3: Find the matching appointment under appointmentsLecturer
                        db.child("appointmentsLecturer").child(lecturerId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(lecturerSnapshot: DataSnapshot) {
                                    for (lecturerAppSnap in lecturerSnapshot.children) {
                                        val lecApp = lecturerAppSnap.getValue(AppointmentLecturer::class.java)
                                        val lecKey = lecturerAppSnap.key ?: continue

                                        // Match by date, time, and studentID
                                        if (lecApp?.date == date &&
                                            lecApp.time == time &&
                                            lecApp.studentId == studentId
                                        ) {
                                            db.child("appointmentsLecturer")
                                                .child(lecturerId)
                                                .child(lecKey)
                                                .updateChildren(updates)
                                            break
                                        }
                                    }

                                    Toast.makeText(this@CancelAppointmentActivity, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                                    finish()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@CancelAppointmentActivity, "Error updating lecturer view", Toast.LENGTH_SHORT).show()
                                }
                            })
                    } else {
                        Toast.makeText(this@CancelAppointmentActivity, "Appointment not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CancelAppointmentActivity, "Error accessing appointment", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
