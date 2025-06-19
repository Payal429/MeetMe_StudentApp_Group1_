package com.group1.meetme

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.group1.meetme.databinding.FragmentStudentBookingsBinding
import java.util.Locale

class studentBookingsFragment : Fragment() {

    // Backing property for view binding to safely access layout views
    private var _binding: FragmentStudentBookingsBinding? = null
    // Non-nullable view binding property for use within the fragment
    private val binding get() = _binding!!

    // Inflate the layout and initialize binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using generated binding class
        _binding = FragmentStudentBookingsBinding.inflate(inflater, container, false)
        // Return the root view of the binding
        return binding.root
    }

    // Called after the fragment's view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved language preference
        loadLanguage()

        // Retrieve user ID and role from shared preferences
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("ID_NUM", "") ?: ""
        val userType = sharedPreferences.getString("USER_ROLE", "") ?: ""

        // Setup ViewPager with fragments
        val fragments = listOf(
            UpcomingAppointmentsFragment.newInstance(userId, userType),
            CompletedAppointmentsFragment.newInstance(userId, userType),
            CancelledAppointmentsFragment.newInstance(userId, userType)
        )

        // Titles for the TabLayout tabs
        val titles = listOf(getString(R.string.upcoming), getString(R.string.completed),
            getString(R.string.cancelled))

        // Get references to ViewPager2 and TabLayout from the binding
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        // Set adapter for ViewPager2 with fragments
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        // Attach TabLayout with ViewPager2 and set tab titles accordingly
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    // Load saved language code from SharedPreferences and apply locale
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en") ?: "en"
        setLocale(savedLanguage)
    }


    // Change app locale/language at runtime using the given language code
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        // Update the resources with the new configuration
        requireContext().resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Clear the binding reference when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}