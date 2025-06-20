package com.group1.meetme

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
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

// Activity for scheduling availability.
class ScheduleAvailabilityActivity : AppCompatActivity() {

    // Define view-related variables and data fields
    private lateinit var holidayDates: List<Calendar>
    private lateinit var calendarView: CalendarView
    private lateinit var dateTextView: TextView
    private lateinit var btnAddAvailability: Button
    private lateinit var database: DatabaseReference
    private var selectedDate: String = getTodayDate()
    private lateinit var timeSlotSpinner: Spinner
    private lateinit var venueSpinner: Spinner

    // Example slot list
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
        "17:20 - 18:00",
//        "21:43 - 21:50",
//        "21:45 - 22:30",
//        "21:47 - 22:40",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_schedule_availability)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the user's ID number from SharedPreferences.
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)
        Log.d("idNum", idNum!!)

        // Initialize UI components.
        dateTextView = findViewById(R.id.dateTextView)
        calendarView = findViewById(R.id.calendarView)
        btnAddAvailability = findViewById(R.id.btnAddAvailability)

        timeSlotSpinner = findViewById(R.id.timeSpinner)
        venueSpinner = findViewById(R.id.venueSpinner)
        val statusText = findViewById<TextView>(R.id.statusText)

        // Set up the time slot spinner.
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSlotSpinner.adapter = adapter

        // Initialize Firebase database reference.
        database = FirebaseDatabase.getInstance().reference

        // Define holiday dates/non-bookable days
        holidayDates = HolidayUtils.holidayDates

        // Add red highlights for holidays
        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light)))
        }
        calendarView.setEvents(events)

        // Generate list of past days from today back to a specific limit (e.g., 2 years)
        val today = Calendar.getInstance()
        val pastDates = mutableListOf<Calendar>()

        val pastLimit = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }

        val dateIterator = pastLimit.clone() as Calendar
        while (dateIterator.before(today)) {
            pastDates.add(dateIterator.clone() as Calendar)
            dateIterator.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Combine holidays and past dates
        val disabledDays = pastDates + holidayDates
        calendarView.setDisabledDays(disabledDays)


        // Show calendar when TextView is clicked
        dateTextView.setOnClickListener {
            calendarView.visibility = View.VISIBLE // Show the calendar
        }


        // Handle date selection from the calendar.
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar

                if (isHoliday(selectedCal)) {
                    Toast.makeText(
                        this@ScheduleAvailabilityActivity,
                        "This day is a holiday and cannot be booked!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate =
                        dateFormat.format(selectedCal.time) // ✅ Fix: update class variable
                    Log.d("date", selectedDate)
                    dateTextView.text = selectedDate
                    calendarView.visibility = View.GONE
                }
            }
        })


        // Handle adding availability.
        btnAddAvailability.setOnClickListener {
            val selectedTimeSlot = timeSlotSpinner.selectedItem.toString()
            val selectedVenue = venueSpinner.selectedItem.toString()

            val date = selectedDate

            val slotRef = FirebaseDatabase.getInstance().reference
                .child("availability")
                .child(idNum!!)
                .child(date)
                .child(selectedTimeSlot)

            // Optional: prevent duplicates
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
                            "venue" to selectedVenue
                        )
                        slotRef.setValue(slotData).addOnSuccessListener {
                            statusText.text = "Slot added: $selectedTimeSlot on $date"
                            statusText.setTextColor(Color.GREEN) // Change Color
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

        // Load saved language preference
        loadLanguage()

    }

    // Load the saved language from SharedPreferences and apply it
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
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

    // Check if selected date matches a holiday
    private fun isHoliday(date: Calendar): Boolean {
        return holidayDates.any {
            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    it.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    it.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Get the current date in "yyyy-MM-dd" format.
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}