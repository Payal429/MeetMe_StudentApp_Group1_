package com.group1.meetme

import com.google.firebase.database.Exclude

//data class Appointment(
//    val lecturerId: String = "",
//    val date: String = "",
//    val time: String = "",
//    val module: String = "",
//    val status: String = "" // "upcoming", "completed", or "cancelled"
//)

// Data class representing an appointment.
data class Appointment(
    var id: String = "",
    var date: String = "",
    var time: String = "",
    var lecturerId: String = "",
    var studentID: String = "",
    var module: String = "",
    var status: String = "",
    var cancellationReason: String = ""
)

// Data class representing an appointment from the lecturer's perspective.
data class AppointmentLecturer(
    var id: String = "",
    var date: String = "",
    var time: String = "",
    var lecturerId: String = "",
    var studentId: String = "",
    var module: String = "",
    var status: String = "",
    var cancellationReason: String = ""
)


