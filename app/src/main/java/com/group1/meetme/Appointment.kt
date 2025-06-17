package com.group1.meetme

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


