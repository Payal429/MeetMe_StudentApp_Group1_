package com.group1.meetme

import com.google.firebase.database.IgnoreExtraProperties

//https://firebase.google.com/docs/database/android/read-and-write
@IgnoreExtraProperties
// Data class to represent a user.
data class User(
    var idNum: String = "",
    var name: String = "",
    var surname: String = "",
    var typeOfUser: String = "",
    var course: String = "",
    var email: String = ""
) {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
