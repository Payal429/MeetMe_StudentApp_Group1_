package com.group1.meetme

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// This is a Kotlin object that acts as a singleton for creating Retrofit instances.
object ApiClient {
    // Define the base URL for the API. This should be updated with the actual base URL of the API.
    private const val BASE_URL = "https://meetme-user-api.onrender.com/" // Update with actual base URL

    // Create a Retrofit instance using a lazy delegate to ensure it is only initialised once.
    private val retrofit: Retrofit by lazy {
        // Build the Retrofit instance with the specified base URL and Gson converter factory.
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Add the Gson converter factory to handle JSON serialization/deserialization.
            .addConverterFactory(GsonConverterFactory.create())
            // Build the Retrofit instance.
            .build()
    }
    // A generic function to create an instance of a service interface.
    fun <T> create(service: Class<T>): T {
        // Use the Retrofit instance to create an instance of the specified service interface
        return retrofit.create(service)
    }
}