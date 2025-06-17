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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.firebase.database.*
import com.group1.meetme.HolidayUtils.holidayDates
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
import java.util.*

// Activity for rescheduling appointments.
class RescheduleActivity : AppCompatActivity() {

    // Define view-related variables and data fields
    private lateinit var calendarView: CalendarView
    private lateinit var timeSlotSpinner: Spinner
    private lateinit var rescheduleButton: Button
    private lateinit var holidayDates: List<Calendar>

    private lateinit var lecturerId: String
    private lateinit var module: String
    private var isRebooking = false
    private lateinit var studentId: String
    private var selectedDate: String = ""
    private var availableTimes: List<String> = emptyList()

    // Data class to represent a time slot.
    data class Slot(val booked: Boolean = false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reschedule)

        // Initialize UI components.
        calendarView = findViewById(R.id.rescheduleCalendarView)
        timeSlotSpinner = findViewById(R.id.timeSlotSpinner)
        rescheduleButton = findViewById(R.id.rescheduleConfirmButton)


        // Retrieve data from the intent.
        lecturerId = intent.getStringExtra("lecturerId") ?: ""
        module = intent.getStringExtra("module") ?: ""
        isRebooking = intent.getBooleanExtra("isRebooking", false)
        // studentId = "student123"

        // Get userId and userType from SharedPreferences or Intent
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        studentId = sharedPreferences.getString("ID_NUM", "") ?: ""
        // val userType = sharedPreferences.getString("USER_ROLE", "") ?: ""

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish() // Optional: finishes current activity so it's removed from back stack
        }

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


        // Set the title based on whether it's a rebooking or rescheduling.
        if (isRebooking) {
            title = "Rebook Appointment - $module"
        } else {
            title = "Reschedule Appointment"
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
                        this@RescheduleActivity,
                        "This day is a holiday and cannot be booked!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else  // Check if the selected date is before today
                    if (selectedCal.before(todayDate)) {
                        Toast.makeText(
                            this@RescheduleActivity,
                            "You cannot select a past date.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    } else {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDate =
                            dateFormat.format(selectedCal.time)

                        Log.d("date", selectedDate)
                        loadAvailableTimesForDate(selectedDate)
                    }
            }
        })

        // Set up the reschedule button click listener.
        rescheduleButton.setOnClickListener {
            val time = timeSlotSpinner.selectedItem?.toString()?.trim() ?: ""
            if (selectedDate.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Select date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isRebooking) {
                createNewAppointment(selectedDate, time)
            } else {
                val appointmentId =
                    intent.getStringExtra("appointmentId") ?: return@setOnClickListener
                updateExistingAppointment(appointmentId, selectedDate, time)
            }
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

    // Function to load available times for the selected date.
    private fun loadAvailableTimesForDate(date: String) {
        val db = FirebaseDatabase.getInstance().reference
        val ref = db.child("availability").child(lecturerId).child(date)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timeList = mutableListOf<String>()
                for (snap in snapshot.children) {
                    val slot = snap.getValue(Slot::class.java)
                    val timeKey = snap.key ?: continue
                    if (slot != null && !slot.booked) {
                        timeList.add(timeKey)
                    }
                }

                if (timeList.isEmpty()) {
                    Toast.makeText(
                        this@RescheduleActivity,
                        "No available times on $date",
                        Toast.LENGTH_SHORT
                    ).show()
                    timeSlotSpinner.adapter = null
                } else {
                    availableTimes = timeList.sorted()
                    val adapter = ArrayAdapter(
                        this@RescheduleActivity,
                        android.R.layout.simple_spinner_item,
                        availableTimes
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    timeSlotSpinner.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@RescheduleActivity,
                    "Error loading times: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Function to create a new appointment.
    private fun createNewAppointment(date: String, time: String) {
        val db = FirebaseDatabase.getInstance().reference
        val newRef = db.child("appointments").child(studentId).push()

        val data = mapOf(
            "date" to date,
            "time" to time,
            "lecturerId" to lecturerId,
            "module" to module,
            "status" to "upcoming"
        )

        newRef.setValue(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment booked for $date at $time", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show()
            }


        // Update the old appointment slot to unbooked.
        val oldtime = intent.getStringExtra("oldTime")
        val olddate = intent.getStringExtra("oldDate")
        Log.d("oldTime", oldtime!!)
        Log.d("oldDate", olddate!!)
        val slotRef =
            FirebaseDatabase.getInstance().reference.child("availability").child(lecturerId!!)
                .child(olddate!!).child(oldtime!!)

        slotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val booked = currentData.child("booked").getValue(Boolean::class.java) ?: false
                return if (!booked) {
                    currentData.child("booked").value = false
                    currentData.child("student").value = ""
                    currentData.child("module").value = ""
                    Transaction.success(currentData)
                } else {
                    Transaction.abort()
                }
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (committed) {
                    // Add an entry for the lecturer appointments
                    val appointmentLecturer = mapOf(
                        "studentID" to studentId,
                        "date" to date,
                        "time" to time,
                        "module" to module,
                        "status" to "upcoming"
                    )

                    val appointmentRefLecturer =
                        FirebaseDatabase.getInstance().reference.child("appointmentsLecturer")
                            .child(lecturerId).push()
                    appointmentRefLecturer.setValue(appointmentLecturer)

                    // Send notification to the lecturer.
                    FirebaseDatabase.getInstance().reference.child("tokens").child(lecturerId).get()
                        .addOnSuccessListener { tokenSnapshot ->
                            val token = tokenSnapshot.getValue(String::class.java)
                            if (token != null) {
                                sendPushyNotification(
                                    token,
                                    "New Appointment Booked",
                                    "Student $studentId booked a slot at $time on $date"
                                )

                                // Schedule a reminder for the appointment.
                                scheduleReminder(date, time, token)
                            }
                        }
                } else {
                    // statusText.text = "Timeslot already booked."
                    Log.d("Already Booked", "Time Already Booked")

                }
            }


        })
    }

    // Function to schedule a reminder for the appointment.
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
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMillis,
                pendingIntent
            )
        }
    }

    // Function to send a Pushy notification.
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

    // Function to update an existing appointment.
    private fun updateExistingAppointment(appointmentId: String, date: String, time: String) {
        val db = FirebaseDatabase.getInstance().reference
        db.child("appointments").child(studentId).child(appointmentId)
            .updateChildren(mapOf("date" to date, "time" to time))
            .addOnSuccessListener {
                Toast.makeText(this, "Rescheduled successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error updating", Toast.LENGTH_SHORT).show()
            }
    }
}
