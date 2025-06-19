package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ScheduleAvailabilityFragment : Fragment() {
    // Define view-related variables and data fields
    private lateinit var holidayDates: List<Calendar>
    private lateinit var calendarView: CalendarView
    private lateinit var dateTextView: TextView
    private lateinit var btnAddAvailability: Button
    private lateinit var database: DatabaseReference
    private var selectedDate: String = getTodayDate()
    private lateinit var timeSlotSpinner: Spinner
    private lateinit var venueSpinner: Spinner
    private lateinit var customVenueEditText: EditText
    private lateinit var statusText: TextView

    // Predefined list of available time slots
    private val timeSlots = listOf(
        "07:10 - 07:50",
        "08:00 - 08:40",
        "08:50 - 09:30",
        "09:40 - 10:20",
        "10:30 - 11:10",
        "11:20 - 12:00",
        "12:10 - 12:40",
        "13:00 - 13:40",
        "14:00 - 14:40",
        "14:50 - 15:30",
        "15:40 - 16:20",
        "16:30 - 17:10",
        "17:20 - 18:00"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_schedule_availability, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get stored lecturer ID number from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null) ?: return

        // Initialize UI elements
        dateTextView = view.findViewById(R.id.dateTextView)
        calendarView = view.findViewById(R.id.calendarView)
        btnAddAvailability = view.findViewById(R.id.btnAddAvailability)
        timeSlotSpinner = view.findViewById(R.id.timeSpinner)
        venueSpinner = view.findViewById(R.id.venueSpinner)
        customVenueEditText = view.findViewById(R.id.customVenueEditText)
        statusText = view.findViewById(R.id.statusText)

        dateTextView.text = getTodayDate()

        // Set up the time slot spinner with predefined values
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSlotSpinner.adapter = adapter

        // Get reference to Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Get holiday dates from HolidayUtils
        holidayDates = HolidayUtils.holidayDates

        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light)))
        }
        calendarView.setEvents(events)

        // Disable all past dates from 2 years ago to today
        val today = Calendar.getInstance()
        val pastDates = mutableListOf<Calendar>()
        val pastLimit = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }
        val dateIterator = pastLimit.clone() as Calendar
        while (dateIterator.before(today)) {
            pastDates.add(dateIterator.clone() as Calendar)
            dateIterator.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Disable both past dates and holiday dates
        val disabledDays = pastDates + holidayDates
        calendarView.setDisabledDays(disabledDays)

        // Show calendar view when the date TextView is clicked
        dateTextView.setOnClickListener { calendarView.visibility = View.VISIBLE }


        // Set up venue spinner from resources
        val venueAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.venues,
            android.R.layout.simple_spinner_item
        )
        venueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        venueSpinner.adapter = venueAdapter

        // Handle venue selection
        venueSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedVenue = parent.getItemAtPosition(position).toString()

                when (selectedVenue) {
                    "Select Venue" -> {
                        // Optional: clear input or display error when trying to submit
                        customVenueEditText.visibility = View.GONE
                    }

                    "Other" -> {
                        // Enable custom venue input
                        customVenueEditText.visibility = View.VISIBLE
                    }

                    else -> {
                        // Hide custom input if not "Other"
                        customVenueEditText.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Handle calendar day selection
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar
                // Prevent booking on holidays
                if (isHoliday(selectedCal)) {
                    Toast.makeText(
                        requireContext(),
                        "This day is a holiday and cannot be booked!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Format and display the selected date
                    selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                    dateTextView.text = selectedDate
                    calendarView.visibility = View.GONE
                }
            }
        })

        // Handle Add Availability button click
        btnAddAvailability.setOnClickListener {
            val selectedTimeSlot = timeSlotSpinner.selectedItem.toString()
            val spinnerVenue = venueSpinner.selectedItem.toString()
            val date = selectedDate.trim()

            // Validate date
            if (date.isEmpty()) {
                dateTextView.setBackgroundResource(R.drawable.border_red)
                Toast.makeText(requireContext(), "Please select a date.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                dateTextView.setBackgroundResource(R.drawable.edit_text_background)
            }

            // Validate time slot selection
            if (selectedTimeSlot == "Select Time" || selectedTimeSlot.isEmpty()) {
                timeSlotSpinner.setBackgroundResource(R.drawable.border_red)
                Toast.makeText(requireContext(), "Please select a valid time slot.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                timeSlotSpinner.setBackgroundResource(R.drawable.edit_text_background)
            }

            val selectedVenue = when {
                spinnerVenue == "Select Venue" -> {
                    venueSpinner.setBackgroundResource(R.drawable.border_red)
                    Toast.makeText(requireContext(), "Please select a venue.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                spinnerVenue == "Other" -> {
                    // Custom venue validation
                    val input = customVenueEditText.text.toString().trim()
                    if (input.isEmpty()) {
                        customVenueEditText.setBackgroundResource(R.drawable.border_red)
                        customVenueEditText.requestFocus()
                        customVenueEditText.error = "This field is required"
                        return@setOnClickListener
                    } else {
                        customVenueEditText.setBackgroundResource(R.drawable.edit_text_background)
                    }

                    input
                }
                else -> {
                    spinnerVenue
                    venueSpinner.setBackgroundResource(R.drawable.edit_text_background)
                }


            }

            // Check if selected time is in the past (only if the date is today)
            val today = Calendar.getInstance()
            val selectedDateCal = Calendar.getInstance()
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

            try {
                val parsedDate = sdfDate.parse(date)
                selectedDateCal.time = parsedDate

                val selectedTimeOnly = selectedTimeSlot.split("-")[0].trim()
                val selectedTime = sdfTime.parse(selectedTimeOnly)

                if (sdfDate.format(today.time) == date) {
                    val currentTime = sdfTime.parse(sdfTime.format(today.time))
                    if (selectedTime.before(currentTime)) {
                        timeSlotSpinner.setBackgroundResource(R.drawable.border_red)
                        Toast.makeText(requireContext(), "You cannot select a time in the past.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error parsing time or date.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            // Show a confirmation dialog before saving
            val confirmationMessage = "Confirm adding slot:\n\nDate: $date\nTime: $selectedTimeSlot\nVenue: $spinnerVenue"

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Availability")
                .setMessage(confirmationMessage)
                .setPositiveButton("Confirm") { _, _ ->
                    val slotRef = FirebaseDatabase.getInstance().reference
                        .child("availability")
                        .child(idNum)
                        .child(date)
                        .child(selectedTimeSlot)

                    // Check if the selected slot already exists
                    slotRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                statusText.text = "Slot already exists"
                                statusText.setTextColor(Color.RED)
                            } else {
                                val slotData = mapOf(
                                    "booked" to false,
                                    "student" to "",
                                    "module" to "",
                                    "venue" to spinnerVenue
                                )
                                slotRef.setValue(slotData).addOnSuccessListener {
                                    statusText.text = "Slot added: $selectedTimeSlot on $date"
                                    statusText.setTextColor(Color.parseColor("#388E3C")) // dark green
                                }.addOnFailureListener {
                                    statusText.text = "Failed to add slot"
                                    statusText.setTextColor(Color.RED)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            statusText.text = "Database error: ${error.message}"
                            statusText.setTextColor(Color.RED)
                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()

            resetAvailabilityPage()
        }
        // Load previously selected language preference
        loadLanguage()
    }

    private fun resetAvailabilityPage() {

        // Reset date to today
        selectedDate = ""
        dateTextView.text = "Select Date"

       // Optionally collapse calendar
        calendarView.visibility = View.GONE

        // Reset status message
        // Hide custom venue field if shown
        customVenueEditText.setText("")
        customVenueEditText.visibility = View.GONE
        venueSpinner.setSelection(0)
        timeSlotSpinner.setSelection(0)

        // Clear status text after a delay
        Handler(Looper.getMainLooper()).postDelayed({
            statusText.text = ""
        }, 3000)
    }

    // Load the saved language from SharedPreferences and apply it
    private fun loadLanguage() {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Apply the specified locale to the app
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Check if the selected date is a holiday
    private fun isHoliday(date: Calendar): Boolean {
        return holidayDates.any {
            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    it.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    it.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Get today's date in yyyy-MM-dd format
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}