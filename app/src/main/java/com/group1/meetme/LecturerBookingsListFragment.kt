package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Context
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.group1.meetme.databinding.FragmentLecturerBookingsListBinding
import com.group1.meetme.databinding.FragmentStudentBookingsBinding
import java.util.Locale

class LecturerBookingsListFragment : Fragment() {

    // View binding variable for this fragment
    private var _binding: FragmentLecturerBookingsListBinding? = null
    private val binding get() = _binding!!

    // Called when the Fragment’s UI is being created
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentLecturerBookingsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called when the Fragment’s UI has been created and is ready to use
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply saved language preference
        loadLanguage()

        // Retrieve user ID and role from shared preferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("ID_NUM", "") ?: ""
        val userType = sharedPreferences.getString("USER_ROLE", "") ?: ""

        // Setup ViewPager with fragments
        val fragments = listOf(
            UpcomingAppointmentsFragment.newInstance(userId, userType),
            CompletedAppointmentsFragment.newInstance(userId, userType),
            CancelledAppointmentsFragment.newInstance(userId, userType)
        )

        // Titles for each tab
        val titles = listOf("Upcoming", "Completed", "Cancelled")

        // Set up the ViewPager with a FragmentStateAdapter
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        // Connect the TabLayout with the ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    // Load the saved language setting from shared preferences and apply it
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en") ?: "en"
        setLocale(savedLanguage)
    }

    // Set the app locale based on the selected language code
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Clear the view binding reference when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}