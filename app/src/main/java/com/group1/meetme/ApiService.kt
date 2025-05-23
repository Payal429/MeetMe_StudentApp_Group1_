package com.group1.meetme

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class representing the request body for onboarding a new user.
data class OnboardRequest(
    val idNum: String,
    val name: String,
    val surname: String,
    val typeOfUser: String,
    val course: String,
    val email: String
)

// Data class representing the request body for verifying OTP.
data class VerifyOtpRequest(val idNum: String, val otp: String)
data class ChangePassword(val idNum: String, val password: String)
data class GetUser(val idNum: String)
data class ApiResponse(val message: String, val error: String? = null)
data class UserTypeResponse(
    val typeOfUser: String
)

// Interface defining the API endpoints and their corresponding HTTP methods.
interface ApiService {

    // Endpoint for onboarding new user
    @POST("onboard")
    fun onboardNewUser(@Body request: OnboardRequest): Call<ApiResponse>

    // Endpoint for subsequent login with new password
    @POST("verify-otp")
    fun loginWithOtp(@Body request: VerifyOtpRequest): Call<ApiResponse>

    // Endpoint for changing password after OTP login
    @POST("set-password")
    fun changePassword(@Body request: ChangePassword): Call<ApiResponse>

    // Endpoint for login
    @POST("login")
    fun login(@Body request: ChangePassword): Call<ApiResponse>

    // Endpoint for getting the user type
//    @POST("get-usertype")
//    fun getUserType(@Body request: GetUser): Call<ApiResponse>

    @POST("get-usertype")
    fun getUserType(@Body request: GetUser): Call<UserTypeResponse>



//    // Endpoint for OTP login (first-time login with OTP)
//    @POST("loginotp")
//    fun loginWithOtp(@Body request: LoginRequest): Call<ApiResponse>
//
//    // Endpoint for subsequent login with new password
//    @POST("login")
//    fun loginWithPassword(@Body request: LoginRequest): Call<ApiResponse>
//
//    // Endpoint for changing password after OTP login
//    @POST("change-password")
//    fun changePassword(@Body request: PasswordChangeRequest): Call<ApiResponse>
}
