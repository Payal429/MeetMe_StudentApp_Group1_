package com.group1.meetme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class LecturerMainActivity : AppCompatActivity() {

    // Declare necessary UI components for layout and navigation
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load layout XML for lecturer main screen
        setContentView(R.layout.activity_lecturer_main)

        // Initialize views
        drawerLayout = findViewById(R.id.lecturer_drawer_layout)
        navView = findViewById(R.id.lecturer_nav_view)
        bottomNav = findViewById(R.id.lecturer_bottom_nav)
        toolbar = findViewById(R.id.lecturer_toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)

        // Set up NavController from NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.lecturer_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomNav = findViewById(R.id.lecturer_bottom_nav)

        // Correctly initialize the class-level appBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.LecturerDashboardFragment,
                R.id.LecturerBookingsListFragment,
                R.id.ScheduleAvailabilityFragment,
                R.id.UploadFragment,
                R.id.SettingsFragment
            ),
            drawerLayout
        )

        // Setup action bar and nav view with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect side NavigationView with NavController for handling nav menu clicks
        navView.setupWithNavController(navController)

        // Connect BottomNavigationView with NavController for bottom menu nav
        bottomNav.setupWithNavController(navController)

        // Handle Logout menu manually
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    // If logout is clicked, show confirmation dialog
                    showLogoutConfirmationDialog()
                    true
                }

                else -> {
                    // For other menu items, let NavController handle the navigation
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) drawerLayout.closeDrawers()
                    handled
                }
            }
        }

        // Show/hide bottom nav based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id == R.id.LecturerDashboardFragment) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    // Handle "up" button (back arrow or hamburger) functionality in toolbar
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    // Displays a confirmation dialog when the user chooses to log out
    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->

                // Clear stored user session in SharedPreferences
                val sharedPref = getSharedPreferences("MyPreferences", MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // Navigate to Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            // "Cancel" just dismisses the dialog
            .setNegativeButton("Cancel", null)

            // Show the dialog
            .show()
    }
}