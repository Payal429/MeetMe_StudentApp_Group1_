package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AppointmentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        // Return back to the dashboard
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        backArrow.setOnClickListener(){
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Get userId and userType from SharedPreferences or Intent
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val userId = sharedPreferences.getString("ID_NUM", "") ?: ""
        val userType = sharedPreferences.getString("USER_ROLE", "") ?: ""

        val fragments = listOf(
            UpcomingAppointmentsFragment.newInstance(userId, userType),
            CompletedAppointmentsFragment.newInstance(userId, userType),
            CancelledAppointmentsFragment.newInstance(userId, userType)
        )

        val titles = listOf("Upcoming", "Completed", "Cancelled")

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }


}