package com.group1.meetme

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
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
import com.group1.meetme.HolidayUtils
import com.group1.meetme.HolidayUtils.holidayDates
import com.group1.meetme.User
import com.group1.meetme.databinding.ActivityBookAppointmentBinding
import com.group1.meetme.databinding.FragmentBookAppointmentsBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class bookAppointmentsFragment : Fragment() {

    // ViewBinding variable for this fragment. _binding is nullable to handle fragment lifecycle cleanly.
    private var _binding: FragmentBookAppointmentsBinding? = null

    // Non-nullable access to the binding (safe to use after onCreateView)
    private val binding get() = _binding!!

    // Firebase Realtime Database reference
    private lateinit var database: DatabaseReference

    // Adapter for displaying available time slots in a ListView or Spinner
    private lateinit var adapter: ArrayAdapter<String>

    // List to store time slots available for booking
    private val availableTimes = mutableListOf<String>()

    // List to store names of lecturers fetched from the database
    private val lecturerNames = mutableListOf<String>()

    // A map linking lecturer names to their corresponding IDs for lookup purposes
    private val lecturerIdMap = mutableMapOf<String, String>()

    // List of holidays or non-bookable calendar dates (to be used to disable those days)
    private var holidayDates = listOf<Calendar>()

    // The selected date for booking, initialized to today's date
    private var selectedDate: String = getTodayDate()

    // Inflate the fragment's layout and initialize the binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentBookAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make sure layout respects system bars (for edge-to-edge layout)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize reference to Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        // Set up adapter to display available time slots in a ListView
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, availableTimes)
        binding.slotsListView.adapter = adapter

        // Load lecturers into spinner
        loadLecturers()

        // Load holidays
        holidayDates = HolidayUtils.holidayDates

        // Highlight holidays in calendar
        val events = holidayDates.map {
            EventDay(it, ColorDrawable(resources.getColor(android.R.color.holo_red_light, null)))
        }
        binding.calendarView.setEvents(events)

        // Disable past dates and holidays in calendar
        val today = Calendar.getInstance()
        val pastLimit = Calendar.getInstance().apply { add(Calendar.YEAR, -2) }
        val pastDates = mutableListOf<Calendar>()

        val dateIterator = pastLimit.clone() as Calendar
        while (dateIterator.before(today)) {
            pastDates.add(dateIterator.clone() as Calendar)
            dateIterator.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Disable both past dates and holidays
        val disabledDays = pastDates + holidayDates
        binding.calendarView.setDisabledDays(disabledDays)

        // When user clicks on the date TextView, show the calendar view
        binding.dateTextView.setOnClickListener {
            binding.calendarView.visibility = View.VISIBLE
        }

        // Handle what happens when user selects a date from the calendar
        binding.calendarView.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                val selectedCal = eventDay.calendar

                // Clear time from current date to compare only dates
                val todayDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Check if selected date is a holiday or in the past
                if (isHoliday(selectedCal)) {
                    Toast.makeText(requireContext(), "This day is a holiday and cannot be booked!", Toast.LENGTH_SHORT).show()
                } else if (selectedCal.before(todayDate)) {
                    Toast.makeText(requireContext(), "You cannot select a past date.", Toast.LENGTH_SHORT).show()
                } else {
                    // Update selected date and hide calendar
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedDate = dateFormat.format(selectedCal.time)
                    Log.d("date", selectedDate)
                    binding.dateTextView.text = selectedDate
                    binding.calendarView.visibility = View.GONE
                }
            }
        })

        // Get user's ID number from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val idNum = sharedPreferences.getString("ID_NUM", null) ?: ""

        // Load slots on button click
        binding.loadSlotsButton.setOnClickListener {
            loadAvailableSlots(idNum)
        }

        // Confirmation dialog before booking
        binding.slotsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedTime = availableTimes[position]
            val selectedLecturerName = binding.lecturerSpinner.selectedItem as String
            val moduleName = binding.moduleNameTv.text.toString().trim()

            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Appointment")
                .setMessage("Do you want to book the slot at $selectedTime on $selectedDate with $selectedLecturerName for $moduleName?")
                .setPositiveButton("Yes") { _, _ ->
                    bookSlot(selectedTime, idNum)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // Clean up binding to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Helper function to check if a selected date is a holiday
    private fun isHoliday(date: Calendar): Boolean {
        return holidayDates.any {
            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    it.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    it.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Load all lecturers from Firebase and populate the lecturer spinner
    private fun loadLecturers() {
        val lecturersRef = database.child("users").child("Lecturer")
        lecturersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerNames.clear()
                lecturerIdMap.clear()

                for (lecturerSnap in snapshot.children) {
                    val lecturer = lecturerSnap.getValue(User::class.java)
                    lecturer?.let {
                        val fullName = "${it.name} ${it.surname}"
                        lecturerNames.add(fullName)
                        // Map full name to ID for easy access
                        lecturerIdMap[fullName] = it.idNum
                    }
                }

                // Populate the spinner with the list of lecturer names
                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    lecturerNames
                )
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.lecturerSpinner.adapter = spinnerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load lecturers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Returns today's date in "yyyy-MM-dd" format
    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Loads available slots for the selected date and lecturer
    private fun loadAvailableSlots(idNum: String) {
        val date = selectedDate
        val selectedLecturerName = binding.lecturerSpinner.selectedItem as String
        val lecturerId = lecturerIdMap[selectedLecturerName] ?: return

        Log.d("LecturerID", lecturerId)
        Log.d("date", date)

        val slotsRef = database.child("availability").child(lecturerId).child(date)

        availableTimes.clear()
        adapter.notifyDataSetChanged()
        binding.statusText.text = "Loading..."

        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val isToday = dateFormat.format(today.time) == date

        // Fetch timeslots from Firebase
        slotsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (slotSnapshot in snapshot.children) {
                    val time = slotSnapshot.key ?: continue
                    val booked = slotSnapshot.child("booked").getValue(Boolean::class.java) ?: true
                    if (!booked) {
                        // If the selected date is today, filter out past times
                        if (isToday) {
                            val currentTime = Calendar.getInstance()
                            val slotTime = parseTimeToCalendar(time)
                            if (slotTime.before(currentTime)) {
                                continue
                            }
                        }
                        availableTimes.add(time)
                        Log.d("availableTimes", time)
                    }
                }

                binding.statusText.text = if (availableTimes.isEmpty()) {
                    "No available timeslots."
                } else {
                    "Select a timeslot to book."
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.statusText.text = "Failed to load slots: ${error.message}"
            }
        })
    }

    // Converts a time string (e.g. "10:00-10:30") to a Calendar object for comparison
    private fun parseTimeToCalendar(timeRange: String): Calendar {
        val now = Calendar.getInstance()
        val startTime = timeRange.split("-")[0].trim()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedTime = timeFormat.parse(startTime) ?: return now

        return Calendar.getInstance().apply {
            time = parsedTime
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
        }
    }

    // Books a slot if it's still available (not already booked)
    private fun bookSlot(time: String, idNum: String) {
        val date = selectedDate
        val selectedLecturerName = binding.lecturerSpinner.selectedItem as String
        val moduleName = binding.moduleNameTv.text.toString().trim()
        val lecturerId = lecturerIdMap[selectedLecturerName] ?: return
        val slotRef = database.child("availability").child(lecturerId).child(date).child(time)

        // Run transaction to prevent double booking
        slotRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val booked = currentData.child("booked").getValue(Boolean::class.java) ?: false
                return if (!booked) {
                    // Set slot as booked and attach student info
                    currentData.child("booked").value = true
                    currentData.child("student").value = idNum
                    currentData.child("module").value = moduleName
                    Transaction.success(currentData)
                } else {
                    // Slot already booked
                    Transaction.abort()
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    binding.statusText.text = "Appointment booked for $time!"
                    loadAvailableSlots(idNum)

                    // Generate appointment ID
                    val appointmentId = database.child("appointments").child(idNum).push().key
                    if (appointmentId == null) {
                        binding.statusText.text = "Failed to generate appointment ID."
                        return
                    }

                    // Create appointment entry for student
                    val appointment = mapOf(
                        "id" to appointmentId,
                        "lecturerId" to lecturerId,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    // Create appointment entry for lecturer
                    val appointmentLecturer = mapOf(
                        "id" to appointmentId,
                        "studentID" to idNum,
                        "date" to date,
                        "time" to time,
                        "module" to moduleName,
                        "status" to "upcoming"
                    )

                    // Save to both student and lecturer paths in Firebase
                    database.child("appointments").child(idNum).child(appointmentId).setValue(appointment)
                    database.child("appointmentsLecturer").child(lecturerId).child(appointmentId).setValue(appointmentLecturer)

                    // Send notification to lecturer
                    database.child("tokens").child(lecturerId).get()
                        .addOnSuccessListener { tokenSnapshot ->
                            val token = tokenSnapshot.getValue(String::class.java)
                            if (token != null) {
                                sendPushyNotification(
                                    token,
                                    "New Appointment Booked",
                                    "Student $idNum booked a slot at $time on $date"
                                )
                                scheduleReminder(date, time, token)
                            }
                        }
                } else {
                    binding.statusText.text = "Timeslot already booked."
                    Log.e("Pushy", "No token found for lecturerId: $lecturerId")
                }
            }
        })
    }

    // Schedules a local reminder notification X hours before the appointment
    private fun scheduleReminder(date: String, time: String, lecturerToken: String) {
        val sharedPrefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val reminderHours = sharedPrefs.getInt("reminderHours", 24)

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val appointmentDate = sdf.parse("$date $time") ?: return

        // Time in millis to fire the reminder
        val reminderTimeMillis = appointmentDate.time - (reminderHours * 60 * 60 * 1000)
        if (reminderTimeMillis < System.currentTimeMillis()) return

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("title", "Confirmed - Upcoming Appointment")
            putExtra("message", "You have an appointment at $time on $date.")
            putExtra("lecturerToken", lecturerToken)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminderTimeMillis.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Handle exact alarm permissions for newer Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                // Request user permission to allow exact alarms
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

        // Apply saved language to reminder message if needed
        loadLanguage()
    }

    // Loads saved app language from SharedPreferences
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")
        setLocale(savedLanguage ?: "en")
    }

    // Sets the app locale (language)
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Sends a push notification using Pushy
    private fun sendPushyNotification(deviceToken: String, title: String, message: String) {
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