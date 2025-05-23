package com.group1.meetme

// Data class to represent a review.
data class Review(
    // Unique identifier for the appointment being reviewed.
    val appointmentId: String = "",
    // Unique identifier for the student who is giving the review.
    val studentId: String = "",
    // Unique identifier for the lecturer who was reviewed.
    val lecturerId: String = "",
    // Rating given by the student (e.g., on a scale of 1 to 5).
    val rating: Float = 0f,
    // Optional comment provided by the student.
    val comment: String = ""
)
