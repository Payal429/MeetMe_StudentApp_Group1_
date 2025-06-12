package com.group1.meetme

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
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
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Activity for booking appointments.
class BookAppointmentActivity : AppCompatActivity() {

    // Variables for managing the calendar and date selection.
    private lateinit var holidayDates: List<Calendar>
    private lateinit var calendarView: CalendarView
    private lateinit var dateTextView: TextView

    // Firebase database reference.
    private lateinit var database: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var statusText: TextView
    private lateinit var moduleNameTv: EditText
    private lateinit var loadButton: Button

    // List to store available times for booking.
    private var selectedDate: String = getTodayDate()
    private val availableTimes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    // Spinner for selecting lecturers.
    private lateinit var lecturerSpinner: Spinner
    private val lecturerNames = mutableListOf<String>()
    private val lecturerIdMap = mutableMapOf<String, String>() // name -> id


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_appointment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        // Find the back arrow button and set an OnClickListener to navigate back to the dashboard.
        backArrow.setOnClickListener() {
//            val intent = Intent(this, StudentDashboardActivity::class.java)
//            startActivity(intent)
            finish()
        }

        // Get the user's ID number from SharedPreferences.
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)

        // Initialize views.
        dateTextView = findViewById(R.id.dateTextView)
        calendarView = findViewById(R.id.calendarView)

        database = FirebaseDatabase.getInstance().reference
        listView = findViewById(R.id.slotsListView)
        statusText = findViewById(R.id.statusText)
        moduleNameTv = findViewById(R.id.moduleNameTv)

        loadButton = findViewById<Button>(R.id.loadSlotsButton)

        // Initialize the adapter for the list of available times.
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availableTimes)
        listView.adapter = adapter

        // Initialize the lecturer spinner.
        lecturerSpinner = findViewById(R.id.lecturerSpinner)
        loadLecturers()

        // Define holiday dates/non-bookable days
        holidayDates = HolidayUtils.holidayDates

        // Add red highlights for holidays
        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light)))
        }
        calendarView.setEvents(events)

        // Set the calendar to show holidays and disable them
        // for (holiday in holidayDates) {
        // calendarView.setDisabledDays(holidayDates)

        // Generate list of past days from today back to a specific limit (e.g., 2 years)
        val today = Calendar.getInstance()
        val pastDates = mutableListOf<Calendar>()

        //val moduleName = moduleNameTv.getText().toString();

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
                val todayDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (isHoliday(selectedCal)) {
                    Toast.makeText(
                        this@BookAppointmentActivity,
                        "This day is a holiday and cannot be booked!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else  // Check if the selected date is before today
                    if (selectedCal.before(todayDate)) {
                        Toast.makeText(
                            this@BookAppointmentActivity,
                            "You cannot select a past date.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
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

        // Load available slots when the load button is clicked.
        loadButton.setOnClickListener {
            loadAvailableSlots(idNum!!)
        }

        // Handle item selection from the list of available times.
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedTime = availableTimes[position]
            bookSlot(selectedTime, idNum!!)
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

    // Load lecturers' names and IDs from the database.
    private fun loadLecturers() {
        val lecturersRef = FirebaseDatabase.getInstance().reference.child("users").child("Lecturer")

        lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerNames.clear()
                lecturerIdMap.clear()

                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(User::class.java)
                    lecturer?.let {
                        val fullName = "${it.name} ${it.surname}"
                        lecturerNames.add(fullName)
                        lecturerIdMap[fullName] = it.idNum
                    }
                }

                val adapter = ArrayAdapter(
                    this@BookAppointmentActivity,
                    android.R.layout.simple_spinner_item,
                    lecturerNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lecturerSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@BookAppointmentActivity,
                    "Failed to load lecturers",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Get the current date in "yyyy-MM-dd" format.
    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Load available time slots for the selected date and lecturer.
    private fun loadAvailableSlots(idNum: String) {
        val date = selectedDate

        val selectedLecturerName = lecturerSpinner.selectedItem as String
        val lecturerId = lecturerIdMap[selectedLecturerName] ?: return
        Log.d("LecturerID", lecturerId)
        Log.d("date", date)

        val slotsRef = database.child("availability").child(lecturerId).child(date)

//        val slotsRef = database.child("availability").child(idNum).child(date)

        availableTimes.clear()
        adapter.notifyDataSetChanged()
        statusText.text = "Loading..."

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val isToday = dateFormat.format(today.time) == date

//        slotsRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (slotSnapshot in snapshot.children) {
//                    val time = slotSnapshot.key ?: continue
//                    val booked = slotSnapshot.child("booked").getValue(Boolean::class.java) ?: true
//
//                    if (!booked) {
//                        // If today, filter out past times
//                        if (isToday) {
//                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//                            val slotTime = Calendar.getInstance().apply {
//                                val parts = time.split(":")
//                                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
//                                set(Calendar.MINUTE, parts[1].toInt())
//                                set(Calendar.SECOND, 0)
//                                set(Calendar.MILLISECOND, 0)
//                            }
//
//                            if (slotTime.before(today)) {
//                                continue // Skip past time
//                            }
//                        }
//
//                        availableTimes.add(time)
//                    }
//                }
//
//                statusText.text = if (availableTimes.isEmpty()) {
//                    "No available timeslots."
//                } else {
//                    "Select a timeslot to book."
//                }
//
//                adapter.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                statusText.text = "Failed to load slots: ${error.message}"
//            }
//        })
//    }

        slotsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (slotSnapshot in snapshot.children) {
                    val time = slotSnapshot.key ?: continue
                    val booked = slotSnapshot.child("booked").getValue(Boolean::class.java) ?: true
                    if (!booked) {
                        // If selected date is today, skip past time slots
                        if (date == getTodayTime()) {
                            val currentTime = Calendar.getInstance()
                            val slotTime = parseTimeToCalendar(time)

                            if (slotTime.before(currentTime)) {
                                continue  // Skip past times
                            }
                        }

                        availableTimes.add(time)
                        Log.d("availableTimes", time)
                    }

                }

                statusText.text = if (availableTimes.isEmpty()) {
                    "No available timeslots."
                } else {
                    "Select a timeslot to book."
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                statusText.text = "Failed to load slots: ${error.message}"
            }
        })
    }

    private fun getTodayTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    private fun parseTimeToCalendar(timeRange: String): Calendar {
        val now = Calendar.getInstance()

        // Extract the start time from the range (e.g., "08:00" from "08:00 - 10:00")
        val startTime = timeRange.split("-")[0].trim()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedTime = timeFormat.parse(startTime) ?: return now

        return Calendar.getInstance().apply {
            time = parsedTime

            // Ensure the date is set to today for accurate comparison
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
        }
    }

    // Book a selected time slot.
    private fun bookSlot(time: String, idNum: String) {
        // Retrieve the selected date from a presumably defined variable
        val date = selectedDate
        // Get the selected lecturer's name from a spinner component
        val selectedLecturerName = lecturerSpinner.selectedItem as String
        // Get the module name from a text view component and trim any extra spaces
        val moduleName = moduleNameTv.text.toString().trim()
        // Retrieve the lecturer's ID from a map using the lecturer's name, or return if not found
        val lecturerId = lecturerIdMap[selectedLecturerName] ?: return
        // Define the reference to the specific slot in the database
        val slotRef = database.child("availability").child(lecturerId).child(date).child(time)

        // Run a transaction on the database reference
        slotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // Check if the slot is already booked
                val booked = currentData.child("booked").getValue(Boolean::class.java) ?: false
                // If the slot is not booked, update the data and commit the transaction
                return if (!booked) {
                    currentData.child("booked").value = true
                    currentData.child("student").value = idNum
                    currentData.child("module").value = moduleName
                    Transaction.success(currentData)
                } else {
                    // Abort the transaction if the slot is already booked
                    Transaction.abort()
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                // If the transaction was committed successfully
                if (committed) {
                    statusText.text = "Appointment booked for $time!"
                    loadAvailableSlots(idNum)

                    // Generate a shared appointment ID
                    val appointmentId = database.child("appointments").child(idNum).push().key
                    if (appointmentId == null) {
                        statusText.text = "Failed to generate appointment ID."
                        return
                    }

                    val appointment = mapOf(
                        "id" to appointmentId,
                        "lecturerId" to lecturerId,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    val appointmentLecturer = mapOf(
                        "id" to appointmentId,
                        "studentID" to idNum,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    // Save under both student and lecturer with the same appointment ID
                    database.child("appointments").child(idNum).child(appointmentId).setValue(appointment)
                    database.child("appointmentsLecturer").child(lecturerId).child(appointmentId).setValue(appointmentLecturer)

                    // Send notification
                    database.child("tokens").child(lecturerId).get()
                        .addOnSuccessListener { tokenSnapshot ->
                            val token = tokenSnapshot.getValue(String::class.java)
                            if (token != null) {
                                sendPushyNotification(
                                    token,
                                    "New Appointment Booked",
                                    "Student $idNum booked a slot at $time on $date"
                                )

                                // Schedule reminder
                                scheduleReminder(date, time, token)
                            }
                        }
                } else {
                    statusText.text = "Timeslot already booked."
                }
            }
        })
    }

    // Schedule a reminder for the booked appointment.
    private fun scheduleReminder(date: String, time: String, lecturerToken: String) {
//        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//        val appointmentDate = sdf.parse("$date $time") ?: return
//
//        val reminderTimeMillis = appointmentDate.time - (24 * 60 * 60 * 1000)
//        if (reminderTimeMillis < System.currentTimeMillis()) return
//
//        val intent = Intent(this, ReminderReceiver::class.java).apply {
//            putExtra("title", "Confirmed - Upcoming Appointment")
//            putExtra("message", "You have an appointment at $time on $date.")
//            putExtra("lecturerToken", lecturerToken)
//        }

        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val reminderHours = sharedPrefs.getInt("reminderHours", 24) // default 24

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val appointmentDate = sdf.parse("$date $time") ?: return

        val reminderTimeMillis = appointmentDate.time - (reminderHours * 60 * 60 * 1000)
        if (reminderTimeMillis < System.currentTimeMillis()) return

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("title", "Confirmed - Upcoming Appointment")
            putExtra("message", "You have an appointment at $time on $date.")
            putExtra("lecturerToken", lecturerToken)
        }


        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderTimeMillis.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Alarm Manager
        // Get the system service for managing alarms
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Check if the device is running Android version S (API level 31) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if the device supports scheduling exact alarms
            if (alarmManager.canScheduleExactAlarms()) {
                // If exact alarms are supported, set an exact alarm that will wake up the device
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, // Use real-time clock to schedule the alarm
                    reminderTimeMillis, // The time at which the alarm should go off
                    pendingIntent // The pending intent to be executed when the alarm triggers
                )
            } else {
                // If exact alarms are not supported, prompt the user to change settings
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                // Set an inexact alarm that allows the device to optimize its use of the doze mode
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
        } else {
            // For devices running versions before Android S, set an exact alarm that allows the device to be woken up
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMillis,
                pendingIntent
            )
        }
    }

    // Send a push notification using Pushy.
    fun sendPushyNotification(deviceToken: String, title: String, message: String) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("to", deviceToken)
            put("data", JSONObject().apply {
                put("title", title)
                put("message", message)
            })
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.pushy.me/push?api_key=e7a666413409280a1c85d6471083b3b933c7e411c6f5ab97251a0e216d8b7696")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Pushy", "Notification failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("Pushy", "Notification sent: ${response.body?.string()}")
            }
        })
    }
}