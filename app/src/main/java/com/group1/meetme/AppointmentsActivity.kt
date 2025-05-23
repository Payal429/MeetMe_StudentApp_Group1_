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

// Activity for displaying appointments.
class AppointmentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the appointments activity layout.
        setContentView(R.layout.activity_appointments)

        // Find the back arrow ImageButton and set an OnClickListener to navigate back to the dashboard.
        val backArrow: ImageButton = findViewById(R.id.backArrow)

        // Create an Intent to navigate back to the StudentDashboardActivity.
        backArrow.setOnClickListener() {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        // Find the TabLayout and ViewPager2 views.
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        // Get userId and userType from SharedPreferences or Intent
        val sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE)
        val userId = sharedPreferences.getString("ID_NUM", "") ?: ""
        val userType = sharedPreferences.getString("USER_ROLE", "") ?: ""

        // Create a list of fragments for the ViewPager2.
        val fragments = listOf(
            UpcomingAppointmentsFragment.newInstance(userId, userType),
            CompletedAppointmentsFragment.newInstance(userId, userType),
            CancelledAppointmentsFragment.newInstance(userId, userType)
        )

        // Create a list of titles for the tabs.
        val titles = listOf("Upcoming", "Completed", "Cancelled")

        // Set up the ViewPager2 adapter.
        viewPager.adapter = object : FragmentStateAdapter(this) {
            // Return the number of fragments.
            override fun getItemCount() = fragments.size

            // Return the fragment at the specified position.
            override fun createFragment(position: Int) = fragments[position]
        }

        // Set up the TabLayout with the ViewPager2.
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set the text for each tab based on the titles list.
            tab.text = titles[position]
        }.attach() // Attach the mediator to the TabLayout and ViewPager2.
    }
}