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

class ScheduleAvailabilityActivity : AppCompatActivity() {

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
        "08:00 - 08:40",
        "08:50 - 09:30",
        "09:40 - 10:20",
        "10:30 - 11:10",
        "11:20 - 12:00",
        "12:10 - 12:40",
        "13:00 - 13:40",
        "14:00 - 14:40",
        "21:43 - 21:50",
        "21:45 - 22:30",
        "21:47 - 22:40",
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

        // get the idnum of the user from the login
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)
        Log.d("idNum", idNum!!)

        dateTextView = findViewById(R.id.dateTextView)
        calendarView = findViewById(R.id.calendarView)
        btnAddAvailability = findViewById(R.id.btnAddAvailability)

        timeSlotSpinner = findViewById(R.id.timeSpinner)
        venueSpinner = findViewById(R.id.venueSpinner)
        val statusText = findViewById<TextView>(R.id.statusText)

        // Set up Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSlotSpinner.adapter = adapter

        database = FirebaseDatabase.getInstance().reference


        // Define holiday dates/non-bookable days
        holidayDates = listOf(
            Calendar.getInstance().apply { set(2025, Calendar.JANUARY, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 21) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 18) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 21) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 27) },

            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 28) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 29) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 30) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 2) },

            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 9) },

            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 22) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 23) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 24) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 25) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 26) },

            Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 25) },
            Calendar.getInstance().apply { set(2025, Calendar.DECEMBER, 26) },

            // Saturdays and Sundays
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 2) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 8) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 9) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 15) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 22) },
            Calendar.getInstance().apply { set(2025, Calendar.FEBRUARY, 23) },

            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 2) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 8) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 9) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 15) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 22) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 23) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 29) },
            Calendar.getInstance().apply { set(2025, Calendar.MARCH, 30) },

            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 5) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 6) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 12) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 13) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 19) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 20) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 26) },
            Calendar.getInstance().apply { set(2025, Calendar.APRIL, 27) },

            Calendar.getInstance().apply { set(2025, Calendar.MAY, 3) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 4) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 10) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 11) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 17) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 18) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 24) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 25) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 31) },

            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 7) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 8) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 14) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 15) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 21) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 22) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 28) },
            Calendar.getInstance().apply { set(2025, Calendar.JUNE, 29) },

            Calendar.getInstance().apply { set(2025, Calendar.JULY, 5) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 6) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 12) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 13) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 19) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 20) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 26) },
            Calendar.getInstance().apply { set(2025, Calendar.JULY, 27) },

            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 2) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 3) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 9) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 10) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 17) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 23) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 24) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 30) },
            Calendar.getInstance().apply { set(2025, Calendar.AUGUST, 31) },

            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 6) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 7) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 13) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 14) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 20) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 21) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 27) },
            Calendar.getInstance().apply { set(2025, Calendar.SEPTEMBER, 28) },

            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 4) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 5) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 11) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 12) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 18) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 19) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 25) },
            Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 26) },

            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 2) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 8) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 9) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 15) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 16) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 22) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 23) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 29) },
            Calendar.getInstance().apply { set(2025, Calendar.NOVEMBER, 30) },

            Calendar.getInstance().apply { set(2026, Calendar.JANUARY, 1) }
        )
// Add red highlights for holidays
        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light)))
        }
        calendarView.setEvents(events)

        // Set the calendar to show holidays and disable them
//        for (holiday in holidayDates) {
        //     calendarView.setDisabledDays(holidayDates)

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


        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar

                if (isHoliday(selectedCal)) {
                    Toast.makeText(this@ScheduleAvailabilityActivity, "This day is a holiday and cannot be booked!", Toast.LENGTH_SHORT).show()
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = dateFormat.format(selectedCal.time) // ✅ Fix: update class variable
                    Log.d("date", selectedDate)
                    dateTextView.text = selectedDate
                    calendarView.visibility = View.GONE
                }
            }
        })


        btnAddAvailability.setOnClickListener {
            val selectedTimeSlot = timeSlotSpinner.selectedItem.toString()
            val selectedVenue = venueSpinner.selectedItem.toString()

            val date = selectedDate

            val slotRef = FirebaseDatabase.getInstance().reference
                .child("availability")
                .child(idNum!!)
                .child(date)
                .child(selectedTimeSlot)
//                .child(selectedVenue)

//            val slotRef = FirebaseDatabase.getInstance().reference
//                .child("availability")
//                .child(lecturerId)
//                .child(date)
//                .child(selectedTime)

            // Optional: prevent duplicates
            slotRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        statusText.text = "Slot already exists"
                        statusText.setTextColor(Color.RED)
                    } else {
//                        val slotData = mapOf("booked" to false)
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
    }

    // Check if selected date matches a holiday
    private fun isHoliday(date: Calendar): Boolean {
        return holidayDates.any {
            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    it.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    it.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }


}