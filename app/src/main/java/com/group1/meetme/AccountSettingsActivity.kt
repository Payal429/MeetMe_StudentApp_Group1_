package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.group1.meetme.databinding.ActivityAboutBinding
import com.group1.meetme.databinding.ActivityAccountSettingsBinding

class AccountSettingsActivity : AppCompatActivity() {

    // binding for the activity
    private lateinit var binding : ActivityAccountSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_account_settings)

        // Configured the binding
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val backArrow = findViewById<ImageView>(R.id.backArrow)

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
