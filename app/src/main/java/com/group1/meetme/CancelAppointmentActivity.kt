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
import com.google.firebase.database.FirebaseDatabase

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

    private fun cancelAppointment(reason: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("appointments").child(studentId).child(appointmentId)
            .updateChildren(mapOf(
                "status" to "cancelled",
                "cancellationReason" to reason
            ))
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel", Toast.LENGTH_SHORT).show()
            }
    }
}
