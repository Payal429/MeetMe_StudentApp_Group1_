package com.group1.meetme

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group1.meetme.databinding.FragmentLecturerDashboardBinding
import com.google.firebase.database.*
import java.util.Locale

class LecturerDashboardFragment : Fragment() {

    // View binding for accessing XML views
    private var _binding: FragmentLecturerDashboardBinding? = null
    private val binding get() = _binding!!

    // ViewModel for handling lecturer dashboard logic
    private lateinit var viewModel: LecturerDashboardViewModel

    // Adapters for the RecyclerViews
    private lateinit var adapterUpcoming: UpcomingAppointmentsAdapter
    private lateinit var adapterPrevious: UpcomingAppointmentsAdapter

    // Lists to store fetched appointments
    private val upcomingAppointments = mutableListOf<Appointment>()
    private val previousAppointments = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout using Data Binding
        _binding = FragmentLecturerDashboardBinding.inflate(inflater, container, false)

        // Get the logged-in user's ID and role
        val userId = getUserIdFromSharedPreferences()
        val userType = getUserTypeFromSharedPreferences()

        // Initialize the ViewModel with a factory that passes the lecturer ID
        val factory = LecturerDashboardViewModelFactory(userId)
        viewModel = ViewModelProvider(this, factory)[LecturerDashboardViewModel::class.java]

        // Bind the ViewModel and Fragment to the layout
        binding.viewModel = viewModel
        binding.fragment = this
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup RecyclerView for upcoming appointments
        adapterUpcoming = UpcomingAppointmentsAdapter(upcomingAppointments, userType)
        binding.appointmentsRecyclerViewUpcoming.layoutManager =
            LinearLayoutManager(requireContext())
        binding.appointmentsRecyclerViewUpcoming.adapter = adapterUpcoming

        // Setup RecyclerView for previous (completed) appointments
        adapterPrevious = UpcomingAppointmentsAdapter(previousAppointments, userType)
        binding.appointmentsRecyclerViewPrevious.layoutManager =
            LinearLayoutManager(requireContext())
        binding.appointmentsRecyclerViewPrevious.adapter = adapterPrevious

        // Load appointment data
        loadUpcomingAppointments(userId)
        loadPreviousAppointments(userId)

        // Set bottom nav listeners
        binding.schedulebutton.setOnClickListener {
            findNavController().navigate(R.id.ScheduleAvailabilityFragment)
        }

        binding.bookingsbutton.setOnClickListener {
            findNavController().navigate(R.id.LecturerBookingsListFragment)
        }

        binding.settingsbutton.setOnClickListener {
            findNavController().navigate(R.id.SettingsFragment)
        }

        // Apply saved language preference
        loadLanguage()

        // Handle back button press with logout confirmation dialog
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showLogoutConfirmation()
                }
            })

        return binding.root
    }

    // Loads upcoming appointments for the lecturer from Firebase Realtime Database
    private fun loadUpcomingAppointments(userId: String) {
        val db = FirebaseDatabase.getInstance().reference
        // Access the appointmentsLecturer node for the current userId
        db.child("appointmentsLecturer").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Clear existing data to avoid duplicates
                    upcomingAppointments.clear()
                    for (snap in snapshot.children) {
                        // Parse each appointment snapshot into Appointment object
                        val appointment = snap.getValue(Appointment::class.java)
                        // Filter for only appointments with status "upcoming"
                        if (appointment?.status == "upcoming") {
                            appointment.id = snap.key ?: ""
                            upcomingAppointments.add(appointment)
                        }
                    }
                    // Notify the adapter that the data has changed so the list refreshes
                    adapterUpcoming.notifyDataSetChanged()
                    binding.noAppointmentsText.visibility =
                        if (upcomingAppointments.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    // Show a toast message if there was an error retrieving data
                    Toast.makeText(
                        requireContext(),
                        "Failed to load upcoming appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Loads previous (completed) appointments for the lecturer from Firebase Realtime Database
    private fun loadPreviousAppointments(userId: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("appointmentsLecturer").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    previousAppointments.clear()
                    for (snap in snapshot.children) {
                        val appointment = snap.getValue(Appointment::class.java)
                        // Filter for appointments with status "completed"
                        if (appointment?.status == "completed") {
                            appointment.id = snap.key ?: ""
                            previousAppointments.add(appointment)
                        }
                    }
                    adapterPrevious.notifyDataSetChanged()
                    binding.noAppointmentsTextPrevious.visibility =
                        if (previousAppointments.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load previous appointments",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    // Function to show logout confirmation asking the user if they want to logout
    private fun showLogoutConfirmation() {
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setTitle("Logout")
        alertDialog.setMessage("Are you sure you want to logout?")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, _ ->
            val sharedPreferences =
                requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("ID_NUM", "").apply()
            requireActivity().finishAffinity()
            dialog.dismiss()
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    // Loads the saved language preference from shared preferences and applies it
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Sets the app locale based on the language code provided
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Retrieves the currently logged-in user's ID from shared preferences
    private fun getUserIdFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("ID_NUM", "") ?: ""
    }

    // Retrieves the currently logged-in user's role/type from shared preferences
    private fun getUserTypeFromSharedPreferences(): String {
        val prefs = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return prefs.getString("USER_ROLE", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}