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

// Custom adapter for displaying a list of appointments.
class AppointmentAdapter(
    private val context: Context,
    private val appointments: List<Appointment>,
    private var userType: String, // "student" or "lecturer"
    private val onAction1: ((Appointment) -> Unit)? = null,
    private val onAction2: ((Appointment) -> Unit)? = null
) : BaseAdapter() {
    // Returns the number of items in the appointments list.
    override fun getCount(): Int = appointments.size

    // Returns the item at the specified position in the list.
    override fun getItem(position: Int): Any = appointments[position]

    // Returns the ID of the item at the specified position.
    override fun getItemId(position: Int): Long = position.toLong()

    // Returns the view for the item at the specified position.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the view if it's not already provided.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_appointment, parent, false)
        // Get the appointment at the current position.
        val appointment = appointments[position]

        // Get user ID and type from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("ID_NUM", null)
        val userRole = sharedPreferences.getString("USER_ROLE", null)!!
        Log.d("userId", userId!!)
        Log.d("userType", userRole!!)

        // Find views by ID.
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
                    btn2.text = "Add Review"
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

        // Check if the user is a lecturer or student and handle reviews accordingly.
        if (userRole == "Lecturer" || userRole == "Student") {
            val reviewBlock = view.findViewById<LinearLayout>(R.id.reviewBlock)
            val ratingBar = view.findViewById<RatingBar>(R.id.reviewRatingBar)
            val commentText = view.findViewById<TextView>(R.id.reviewComment)
            val btn2 = view.findViewById<Button>(R.id.btnAction2) // or whatever ID you used

            // Reference to the Firebase database for reviews.
            val reviewRef = FirebaseDatabase.getInstance().reference
                .child("reviews").child(appointment.id)

            // Add a listener to fetch review data.
            reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // If a review exists, display it.
                    if (snapshot.exists()) {
                        val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0f
                        val comment = snapshot.child("comment").getValue(String::class.java) ?: ""

                        ratingBar.rating = rating
                        commentText.text = if (comment.isNotEmpty()) comment else "No comment"
                        reviewBlock.visibility = View.VISIBLE

                        // Hide the "Leave Review" button if a review exists.
                        btn2?.visibility = View.GONE

                    } else {
                        // If no review exists, hide the review block and show the "Leave Review" button.
                        if (userRole == "Student") {
                            reviewBlock.visibility = View.GONE
                            btn2?.visibility = View.VISIBLE
                        } else {
                            reviewBlock.visibility = View.GONE
                            btn2?.visibility = View.GONE
                        }
                    }
                }
                // Handle errors.
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
        // Return the view for the current item.
        return view
    }
}
