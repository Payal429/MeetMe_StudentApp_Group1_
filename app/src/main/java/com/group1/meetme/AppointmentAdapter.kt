package com.group1.meetme

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AppointmentAdapter(
    private val context: Context,
    private val appointments: List<Appointment>,
    private var userType: String, // "student" or "lecturer"
    private val onAction1: ((Appointment) -> Unit)? = null,
    private val onAction2: ((Appointment) -> Unit)? = null
) : BaseAdapter() {

    override fun getCount(): Int = appointments.size
    override fun getItem(position: Int): Any = appointments[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_appointment, parent, false)

        val appointment = appointments[position]

        // Get user ID and type from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
       val userId = sharedPreferences.getString("ID_NUM", null)
       val  userRole = sharedPreferences.getString("USER_ROLE", null)!!
        Log.d("userId", userId!!)
        Log.d("userType", userRole!!)


        val infoText = view.findViewById<TextView>(R.id.infoText)
        val actionLayout = view.findViewById<LinearLayout>(R.id.actionButtons)
        val btn1 = view.findViewById<Button>(R.id.btnAction1)
        val btn2 = view.findViewById<Button>(R.id.btnAction2)

        // Set info text based on user type
        infoText.text = if (userRole == "Student") {
            "Lecturer: ${appointment.lecturerId}\n" +
                    "Date: ${appointment.date} ${appointment.time}\n" +
                    "Module: ${appointment.module}"
        } else {
            "Student: ${appointment.studentID}\n" +
                    "Date: ${appointment.date} ${appointment.time}\n" +
                    "Module: ${appointment.module}"
        }

        // Show/hide action buttons based on user type and appointment status
        if (userRole == "Student") {
            when (appointment.status) {
                "upcoming" -> {
                    actionLayout.visibility = View.VISIBLE
                    btn1.text = "Cancel"
                    btn2.text = "Reschedule"
                    btn1.setOnClickListener { onAction1?.invoke(appointment) }
                    btn2.setOnClickListener { onAction2?.invoke(appointment) }
                }
                "completed" -> {
                    actionLayout.visibility = View.VISIBLE
//                    btn1.text = "Rebook"
                    btn1.visibility = View.GONE
                    btn2.text = "Review"
                    btn1.setOnClickListener { onAction1?.invoke(appointment) }
                    btn2.setOnClickListener { onAction2?.invoke(appointment) }
                }
                "cancelled" -> {
                    actionLayout.visibility = View.GONE
                }
            }
        } else {
            // Lecturer: Hide buttons
            actionLayout.visibility = View.GONE
        }

        if (userRole == "Lecturer" || userRole == "Student") {
            val reviewBlock = view.findViewById<LinearLayout>(R.id.reviewBlock)
            val ratingBar = view.findViewById<RatingBar>(R.id.reviewRatingBar)
            val commentText = view.findViewById<TextView>(R.id.reviewComment)
            val btn2 = view.findViewById<Button>(R.id.btnAction2) // or whatever ID you used

            val reviewRef = FirebaseDatabase.getInstance().reference
                .child("reviews").child(appointment.id)

            reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0f
                        val comment = snapshot.child("comment").getValue(String::class.java) ?: ""

                        ratingBar.rating = rating
                        commentText.text = if (comment.isNotEmpty()) comment else "No comment"
                        reviewBlock.visibility = View.VISIBLE

                        btn2?.visibility = View.GONE
                    } else {
                        if (userRole == "Student") {
                            reviewBlock.visibility = View.GONE
                            btn2?.visibility = View.VISIBLE
                        } else {
                            reviewBlock.visibility = View.GONE
                            btn2?.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    reviewBlock.visibility = View.GONE
                    btn2?.visibility = if (userType == "Student") View.VISIBLE else View.GONE
                }
            })
        }


//        val reviewBlock = view.findViewById<LinearLayout>(R.id.reviewBlock)
//        val ratingBar = view.findViewById<RatingBar>(R.id.reviewRatingBar)
//        val commentText = view.findViewById<TextView>(R.id.reviewComment)
////        val reviewButton = view.findViewById<Button>(R.id.btnAction2)
//
//        val reviewRef = FirebaseDatabase.getInstance().reference
//            .child("reviews").child(appointment.id)
//
//        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    // Show review block
//                    val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0f
//                    val comment = snapshot.child("comment").getValue(String::class.java) ?: ""
//
//                    ratingBar.rating = rating
//                    commentText.text = if (comment.isNotEmpty()) comment else "No comment"
//                    reviewBlock.visibility = View.VISIBLE
//
//                    // Hide the "Leave Review" button
//                    btn2.visibility = View.GONE
//                } else {
//                    reviewBlock.visibility = View.GONE
//                    btn2.visibility = View.VISIBLE
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                reviewBlock.visibility = View.GONE
//                btn2.visibility = View.VISIBLE
//            }
//        })


        return view




    }
}
