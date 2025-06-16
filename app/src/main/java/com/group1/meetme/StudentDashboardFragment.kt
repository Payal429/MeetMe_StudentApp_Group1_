package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
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

class StudentDashboardFragment : Fragment() {
private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UpcomingAppointmentsAdapter
    private val upcomingAppointments = mutableListOf<Appointment>()

    private lateinit var scheduleButton: ImageButton
    private lateinit var bookingsButton: ImageButton
    private lateinit var resourceButton: ImageButton
    private lateinit var settingsButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_student_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        scheduleButton = view.findViewById(R.id.schedulebutton)
        bookingsButton = view.findViewById(R.id.bookingsbutton)
        settingsButton = view.findViewById(R.id.settingsbutton)

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.appointmentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        adapter = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        recyclerView.adapter = adapter

        loadUpcomingAppointments(userId, userType)

        // Navigation listeners
        scheduleButton.setOnClickListener {
//            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
            findNavController().navigate(R.id.studentBookingFragment)
        }

        bookingsButton.setOnClickListener {
//            startActivity(Intent(requireContext(), AppointmentsActivity::class.java))
            findNavController().navigate(R.id.studentBookingsListFragment)
        }

        settingsButton.setOnClickListener {
            //startActivity(Intent(requireContext(), SettingsActivity::class.java))
            findNavController().navigate(R.id.SettingsFragment)
        }

        // Language support
        loadLanguage()
    }

    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

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
                    Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getUserIdFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }

    // Optional: Add logout confirmation logic if tied to a logout button.
    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to Logout?")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->
            val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("ID_NUM", "").apply()
            requireActivity().finishAffinity()
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()

    }
}