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

    private var _binding: FragmentStudentBookingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved language preference
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

        val titles = listOf("Upcoming", "Completed", "Cancelled")

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en") ?: "en"
        setLocale(savedLanguage)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}