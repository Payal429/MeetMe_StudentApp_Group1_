package com.group1.meetme

import com.google.firebase.database.Exclude

//data class Appointment(
//    val lecturerId: String = "",
//    val date: String = "",
//    val time: String = "",
//    val module: String = "",
//    val status: String = "" // "upcoming", "completed", or "cancelled"
//)

data class Appointment(
    var id: String = "",
    var date: String = "",
    var time: String = "",
    var lecturerId: String = "",
    var module: String = "",
    var status: String = "",
    var cancellationReason: String = ""
)


