package com.group1.meetme

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GetStartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply animations to views
        findViewById<ImageView>(R.id.logo).startAnimation(fadeIn)
        findViewById<TextView>(R.id.textViewSubtitle).startAnimation(slideUp)
        findViewById<ImageView>(R.id.elephant).startAnimation(slideUp)
        findViewById<Button>(R.id.buttonGetStarted).startAnimation(slideUp)
    }
}