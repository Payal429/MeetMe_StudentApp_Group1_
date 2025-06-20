package com.group1.meetme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StudentDashboardViewModelFactory(private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is StudentDashboardViewModel
        if (modelClass.isAssignableFrom(StudentDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Return a new instance with the studentId injected
            return StudentDashboardViewModel(userId) as T
        }
        // Throw an error if an unknown ViewModel is requested
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}