package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
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

class ScheduleAvailabilityFragment : Fragment() {
private lateinit var holidayDates: List<Calendar>
    private lateinit var calendarView: CalendarView
    private lateinit var dateTextView: TextView
    private lateinit var btnAddAvailability: Button
    private lateinit var database: DatabaseReference
    private var selectedDate: String = getTodayDate()
    private lateinit var timeSlotSpinner: Spinner
    private lateinit var venueSpinner: Spinner

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
        return inflater.inflate(R.layout.fragment_schedule_availability, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null) ?: return

        dateTextView = view.findViewById(R.id.dateTextView)
        calendarView = view.findViewById(R.id.calendarView)
        btnAddAvailability = view.findViewById(R.id.btnAddAvailability)
        timeSlotSpinner = view.findViewById(R.id.timeSpinner)
        venueSpinner = view.findViewById(R.id.venueSpinner)
        val statusText = view.findViewById<TextView>(R.id.statusText)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeSlots)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeSlotSpinner.adapter = adapter

        database = FirebaseDatabase.getInstance().reference
        holidayDates = HolidayUtils.holidayDates

        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light)))
        }
        calendarView.setEvents(events)

        val today = Calendar.getInstance()
        val pastDates = mutableListOf<Calendar>()
        val pastLimit = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }

        val dateIterator = pastLimit.clone() as Calendar
        while (dateIterator.before(today)) {
            pastDates.add(dateIterator.clone() as Calendar)
            dateIterator.add(Calendar.DAY_OF_MONTH, 1)
        }

        val disabledDays = pastDates + holidayDates
        calendarView.setDisabledDays(disabledDays)

        dateTextView.setOnClickListener { calendarView.visibility = View.VISIBLE }

        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar
                if (isHoliday(selectedCal)) {
                    Toast.makeText(requireContext(), "This day is a holiday and cannot be booked!", Toast.LENGTH_SHORT).show()
                } else {
                    selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCal.time)
                    dateTextView.text = selectedDate
                    calendarView.visibility = View.GONE
                }
            }
        })

//        btnAddAvailability.setOnClickListener {
//            val selectedTimeSlot = timeSlotSpinner.selectedItem.toString()
//            val selectedVenue = venueSpinner.selectedItem.toString()
//            val date = selectedDate
//
//            val slotRef = database.child("availability").child(idNum).child(date).child(selectedTimeSlot)
//
//            slotRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        statusText.text = "Slot already exists"
//                        statusText.setTextColor(Color.RED)
//                    } else {
//                        val slotData = mapOf(
//                            "booked" to false,
//                            "student" to "",
//                            "module" to "",
//                            "venue" to selectedVenue
//                        )
//                        slotRef.setValue(slotData).addOnSuccessListener {
//                            statusText.text = "Slot added: $selectedTimeSlot on $date"
//                            statusText.setTextColor(Color.GREEN)
//                        }.addOnFailureListener {
//                            statusText.text = "Failed to add slot"
//                            statusText.setTextColor(Color.RED)
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    statusText.text = "Database error: ${error.message}"
//                    statusText.setTextColor(Color.RED)
//                }
//            })
//        }
        btnAddAvailability.setOnClickListener {
            val selectedTimeSlot = timeSlotSpinner.selectedItem.toString()
            val selectedVenue = venueSpinner.selectedItem.toString()
            val date = selectedDate

            val confirmationMessage = "Confirm adding slot:\n\nDate: $date\nTime: $selectedTimeSlot\nVenue: $selectedVenue"

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm Availability")
                .setMessage(confirmationMessage)
                .setPositiveButton("Confirm") { _, _ ->
                    val slotRef = FirebaseDatabase.getInstance().reference
                        .child("availability")
                        .child(idNum)
                        .child(date)
                        .child(selectedTimeSlot)

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
        }


        loadLanguage()
    }

    private fun loadLanguage() {
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
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