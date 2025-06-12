plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.group1.meetme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.group1.meetme"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.google.firebase:firebase-database:20.2.0")
    implementation ("com.google.firebase:firebase-messaging:23.2.0")
    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")
    //implementation ("com.google.android.material:material:1.9.0")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))

    implementation ("com.cloudinary:cloudinary-android:2.3.1")

    // Pushy for push notifications
    implementation ("me.pushy:sdk:1.0.73")

    // OkHttp for HTTP requests
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    // Kotlin JSON handling
    implementation ("org.json:json:20210307")

    // Calendar view for scheduling appointments
    implementation ("com.applandeo:material-calendar-view:1.9.2")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.0.0")

//    // Graphs
//    implementation ("com.github.PhilJay:MPAndroidChart:3.1.0")

}