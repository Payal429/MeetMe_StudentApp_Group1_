package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.google.android.material.card.MaterialCardView
import com.group1.meetme.databinding.FragmentDownloadResourcesBinding
import org.json.JSONObject
import java.util.Locale

class DownloadResourcesFragment : Fragment() {
    private var _binding: FragmentDownloadResourcesBinding? = null
    private val binding get() = _binding!!

    private lateinit var moduleDropdown: AutoCompleteTextView
    private lateinit var resourcesContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadResourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moduleDropdown = binding.moduleDropdown
        resourcesContainer = binding.resourcesContainer

        setupModuleDropdown()
        loadLanguage()
    }

    private fun setupModuleDropdown() {
        val modules = listOf("Module A", "Module B", "Module C")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modules)
        moduleDropdown.setAdapter(adapter)

        moduleDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedModule = modules[position]
            fetchResourcesForModule(selectedModule)
        }
    }

    private fun fetchResourcesForModule(module: String) {
        resourcesContainer.removeAllViews()

        Thread {
            try {
                val result = MediaManager.get().cloudinary.search()
                    .expression("tags=$module")
                    .maxResults(100)
                    .execute()

                val json = JSONObject(result.toString())
                val resources = json.getJSONArray("resources")

                requireActivity().runOnUiThread {
                    if (resources.length() == 0) {
                        Toast.makeText(requireContext(), "No files found.", Toast.LENGTH_SHORT)
                            .show()
                        return@runOnUiThread
                    }

                    for (i in 0 until resources.length()) {
                        val resource = resources.getJSONObject(i)
                        val secureUrl = resource.getString("secure_url")
                        val publicId = resource.getString("public_id")
                        val filename = publicId.substringAfterLast("/")
                        addResourceCard(filename, secureUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching files: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun addResourceCard(fileName: String, fileUrl: String) {
        val context = requireContext()

        val cardView = MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
            radius = 8f
            cardElevation = 2f
        }

        val cardLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val iconView = ImageView(context).apply {
            setImageResource(R.drawable.ic_document)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply { marginEnd = 16 }
        }

        val textView = TextView(context).apply {
            text = fileName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1).apply { weight = 1f }
        }

        val downloadIcon = ImageView(context).apply {
            setImageResource(R.drawable.ic_download)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                startActivity(intent)
            }
        }

        cardLayout.addView(iconView)
        cardLayout.addView(textView)
        cardLayout.addView(spacer)
        cardLayout.addView(downloadIcon)
        cardView.addView(cardLayout)
        resourcesContainer.addView(cardView)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

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
