package com.group1.meetme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Factory class for creating instances of LecturerDashboardViewModel with a non-default constructor (requires lecturerId).
class LecturerDashboardViewModelFactory(private val lecturerId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class is LecturerDashboardViewModel
        if (modelClass.isAssignableFrom(LecturerDashboardViewModel::class.java)) {
            // Return a new instance with the lecturerId injected
            return LecturerDashboardViewModel(lecturerId) as T
        }
        // Throw an error if an unknown ViewModel is requested
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
