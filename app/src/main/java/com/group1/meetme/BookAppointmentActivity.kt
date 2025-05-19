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

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var holidayDates: List<Calendar>
    private lateinit var calendarView: CalendarView
    private lateinit var dateTextView: TextView

    private lateinit var database: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var statusText: TextView
    private lateinit var moduleNameTv: EditText
    private lateinit var loadButton: Button

    private var selectedDate: String = getTodayDate()
    private val availableTimes = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

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

        // get the idnum of the user from the login
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null)

        dateTextView = findViewById(R.id.dateTextView)
        calendarView = findViewById(R.id.calendarView)

        database = FirebaseDatabase.getInstance().reference
        listView = findViewById(R.id.slotsListView)
        statusText = findViewById(R.id.statusText)
        moduleNameTv = findViewById(R.id.moduleNameTv)

        loadButton = findViewById<Button>(R.id.loadSlotsButton)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availableTimes)
        listView.adapter = adapter

        lecturerSpinner = findViewById(R.id.lecturerSpinner)
        loadLecturers()


        // Define holiday dates/non-bookable days
        holidayDates = listOf(
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 1) },
            Calendar.getInstance().apply { set(2025, Calendar.MAY, 15) }
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

        // Handle date selection from calendar
//        calendarView.setOnDayClickListener(object : OnDayClickListener {
//            override fun onDayClick(eventDay: EventDay) {
//                val selectedDate = eventDay.calendar
//
//                if (isHoliday(selectedDate)) {
//                    Toast.makeText(this@BookAppointmentActivity, "This day is a holiday and cannot be booked!", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Format the selected date and set it to the TextView
//                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                    val formattedDate = dateFormat.format(selectedDate.time)
//
//                    dateTextView.text = formattedDate
//                    calendarView.visibility = View.GONE // Hide the calendar once a date is selected
//                }
//            }
//        })
        calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar

                if (isHoliday(selectedCal)) {
                    Toast.makeText(this@BookAppointmentActivity, "This day is a holiday and cannot be booked!", Toast.LENGTH_SHORT).show()
                } else {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = dateFormat.format(selectedCal.time) // ✅ Fix: update class variable

                    Log.d("date", selectedDate)
                    dateTextView.text = selectedDate
                    calendarView.visibility = View.GONE
                }
            }
        })


        loadButton.setOnClickListener {
            loadAvailableSlots(idNum!!)
        }

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

                val adapter = ArrayAdapter(this@BookAppointmentActivity, android.R.layout.simple_spinner_item, lecturerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lecturerSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookAppointmentActivity, "Failed to load lecturers", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

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

        slotsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (slotSnapshot in snapshot.children) {
                    val time = slotSnapshot.key ?: continue
                    val booked = slotSnapshot.child("booked").getValue(Boolean::class.java) ?: true
                    if (!booked) {
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


    private fun bookSlot(time: String, idNum: String) {
        val date = selectedDate
        val selectedLecturerName = lecturerSpinner.selectedItem as String
        val moduleName = moduleNameTv.text.toString().trim()

        val lecturerId = lecturerIdMap[selectedLecturerName] // Use this in your booking logic

//        val moduleName =

        val slotRef = database.child("availability").child(lecturerId!!).child(date).child(time)

        slotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val booked = currentData.child("booked").getValue(Boolean::class.java) ?: false
                return if (!booked) {
                    currentData.child("booked").value = true
                    currentData.child("student").value = idNum
                    currentData.child("module").value = moduleName
                    Transaction.success(currentData)
                } else {
                    Transaction.abort()
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    statusText.text = "Appointment booked for $time!"
                    loadAvailableSlots(idNum)

                    val appointment = mapOf(
                        "lecturerId" to lecturerId,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    val appointmentRef = database.child("appointments").child(idNum).push()
                    appointmentRef.setValue(appointment)


                    // Add an entry for the lecturer appointments
                    val appointmentLecturer = mapOf(
                        "studentID" to idNum,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    val appointmentRefLecturer = database.child("appointmentsLecturer").child(lecturerId).push()
                    appointmentRefLecturer.setValue(appointmentLecturer)

                    // Send notification
                    database.child("tokens").child(lecturerId).get().addOnSuccessListener { tokenSnapshot ->
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

    private fun scheduleReminder(date: String, time: String, lecturerToken: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val appointmentDate = sdf.parse("$date $time") ?: return

        val reminderTimeMillis = appointmentDate.time - (2 * 60 * 60 * 1000)
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

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent)
        }
    }

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