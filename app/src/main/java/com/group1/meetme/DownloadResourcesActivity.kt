package com.group1.meetme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.json.JSONObject
import java.util.Locale

// Activity that handles downloading resources uploaded to Cloudinary
class DownloadResourcesActivity : AppCompatActivity() {

    // UI components
    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var resourcesContainer: LinearLayout
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout for this activity
        setContentView(R.layout.activity_download_resources)

        // Initialize UI elements
        moduleDropdown = findViewById(R.id.moduleDropdown)
        resourcesContainer = findViewById(R.id.resourcesContainer)
        backButton = findViewById(R.id.backArrow)

        // Navigate back to the StudentDashboardActivity when the back arrow is clicked
        backButton.setOnClickListener {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        // Set up dropdown menu with module names
        setupModuleDropdown()

        // Apply user's saved language preference
        loadLanguage()
    }

    // Loads and applies the saved language from SharedPreferences
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Updates the app locale (language)
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Initializes the module dropdown with predefined module names and handles selection
    private fun setupModuleDropdown() {
        // List of modules (can be expanded as needed)
        val modules = listOf("Module A", "Module B", "Module C")

        // Adapter to bind module list to dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)
        moduleDropdown.setAdapter(adapter)

        // When user selects a module, fetch associated resources
        moduleDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedModule = modules[position]
            fetchResourcesForModule(selectedModule)
        }
    }

    // Fetches files uploaded to Cloudinary with a specific tag (module name)
    private fun fetchResourcesForModule(module: String) {
        // Clear the list of any previously displayed resources
        resourcesContainer.removeAllViews()

        // Use a background thread for network operation
        Thread {
            try {
                // Cloudinary search using the tag = module name
                val result = MediaManager.get().cloudinary.search()
                    .expression("tags=$module")
                    .maxResults(100)
                    .execute()

                // Parse result into JSON
                val json = JSONObject(result.toString())
                val resources = json.getJSONArray("resources")

                // Run UI changes on main thread
                runOnUiThread {
                    if (resources.length() == 0) {
                        // Inform user if no files were found
                        Toast.makeText(this, "No files found.", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // Loop through each resource and create a UI card for it
                    for (i in 0 until resources.length()) {
                        val resource = resources.getJSONObject(i)
                        val secureUrl = resource.getString("secure_url")
                        val publicId = resource.getString("public_id")

                        // Extract the filename from the public_id
                        val filename = publicId.substringAfterLast("/")
                        addResourceCard(filename, secureUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Show error toast if something goes wrong
                runOnUiThread {
                    Toast.makeText(this, "Error fetching files: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // Dynamically creates a card view to represent a downloadable file
    private fun addResourceCard(fileName: String, fileUrl: String) {
        // Create card view container
        val cardView = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            radius = 8f
            cardElevation = 2f
        }

        // Create a horizontal layout inside the card
        val cardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        // Icon representing a document
        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_document)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginEnd = 16
            }
        }

        // Text showing the name of the file
        val textView = TextView(this).apply {
            text = fileName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Spacer to push the download icon to the end of the row
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                weight = 1f
            }
        }

        // Download icon that opens the file link
        val downloadIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_download)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp)

            // When clicked, open the file URL in the browser or relevant app
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                startActivity(intent)
            }
        }

        // Add views to the horizontal layout
        cardLayout.addView(iconView)
        cardLayout.addView(textView)
        cardLayout.addView(spacer)
        cardLayout.addView(downloadIcon)

        // Add layout to card, and card to the container
        cardView.addView(cardLayout)
        resourcesContainer.addView(cardView)
    }

    // Extension function to convert dp to pixels (for consistent UI scaling)
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
