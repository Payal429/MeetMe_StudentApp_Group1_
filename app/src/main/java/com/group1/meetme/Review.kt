package com.group1.meetme

data class Review(
    val appointmentId: String = "",
    val studentId: String = "",
    val lecturerId: String = "",
    val rating: Float = 0f,
    val comment: String = ""
)
