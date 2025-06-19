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
    var cancellationReason: String = "",
    var venue: String = ""
)