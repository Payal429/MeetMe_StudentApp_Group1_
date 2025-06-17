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

class StudentMainActivity : AppCompatActivity() {
    // Declare view and navigation variables
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_main)

        // Initialize views by finding them from the layout
        drawerLayout = findViewById(R.id.student_drawer_layout)
        navView = findViewById(R.id.student_nav_view)
        bottomNav = findViewById(R.id.student_bottom_nav)
        toolbar = findViewById(R.id.student_toolbar)

        // Set up toolbar
        setSupportActionBar(toolbar)

        // Set up NavController from NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.student_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Correctly initialize the class-level appBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.studentDashboardFragment,
                R.id.studentBookingFragment,
                R.id.studentBookingsListFragment,
                R.id.studentDownloadFragment,
                R.id.SettingsFragment
            ),
            drawerLayout
        )

        // Setup action bar and nav view with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomNav.setupWithNavController(navController)

        // Handle the Logout menu item manually because it needs a confirmation dialog
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.logout -> {
                    // Show confirmation dialog on logout
                    showLogoutConfirmationDialog()
                    true
                }
                else -> {
                    // For other items, navigate to the selected destination
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) drawerLayout.closeDrawers()
                    handled
                }
            }
        }

        //  Show/hide bottom nav depending on fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.visibility = if (destination.id == R.id.studentDashboardFragment) {
                // Show bottom nav only on dashboard fragment
                View.VISIBLE
            } else {
                // Hide bottom nav on all other fragments
                View.GONE
            }
        }
    }

    // Handle Up button behavior with NavController and DrawerLayout integration
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    // Show a confirmation dialog before logging out the user
    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->

                // Clear any saved session data/preferences
                val sharedPref = getSharedPreferences("MyPreferences", MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // Redirect to LoginActivity and clear back stack
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}