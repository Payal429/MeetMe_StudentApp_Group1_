package com.group1.meetme

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

// Activity for downloading resources.
class DownloadResourcesActivity : AppCompatActivity() {

    // UI components.
    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var resourcesContainer: LinearLayout
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the activity_download_resources layout.
        setContentView(R.layout.activity_download_resources)

        // Initialize UI components.
        moduleDropdown = findViewById(R.id.moduleDropdown)
        resourcesContainer = findViewById(R.id.resourcesContainer)
        backButton = findViewById(R.id.backArrow) //

        // Set up the back button to navigate back to the StudentDashboardActivity.
        backButton.setOnClickListener {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        // Set up the module dropdown
        setupModuleDropdown()
    }

    // Function to set up the module dropdown.
    private fun setupModuleDropdown() {
        // List of available modules.
        val modules = listOf("Module A", "Module B", "Module C")
        // Create an adapter for the dropdown.
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)
        moduleDropdown.setAdapter(adapter)

        // Set up the item click listener for the dropdown.
        moduleDropdown.setOnItemClickListener { _, _, position, _ ->
            // Get the selected module.
            val selectedModule = modules[position]
            // Fetch resources for the selected module.
            fetchResourcesForModule(selectedModule)
        }
    }

    // Function to fetch resources for the selected module.
    private fun fetchResourcesForModule(module: String) {
        // Clear any existing resources from the container.
        resourcesContainer.removeAllViews()

        // Start a new thread to fetch resources from Cloudinary.
        Thread {
            // Perform a search query on Cloudinary to find resources tagged with the module name.
            try {
                val result = MediaManager.get().cloudinary.search()
                    .expression("tags=$module")
                    .maxResults(100)
                    .execute()

                // Parse the JSON response.
                val json = JSONObject(result.toString())
                val resources = json.getJSONArray("resources")

                // Update the UI on the main thread
                runOnUiThread {
                    if (resources.length() == 0) {
                        // Show a toast if no files are found.
                        Toast.makeText(this, "No files found.", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // Iterate through the resources and add them to the UI.
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
                runOnUiThread {
                    Toast.makeText(this, "Error fetching files: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // Function to add a resource card to the resources container.
    private fun addResourceCard(fileName: String, fileUrl: String) {
        // Create a MaterialCardView to hold the resource information.
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

        // Create a LinearLayout to hold the card content.
        val cardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        // Create an icon view for the resource.
        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_document)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginEnd = 16
            }
        }

        // Create a text view for the file name
        val textView = TextView(this).apply {
            text = fileName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create a spacer view.
        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                weight = 1f
            }
        }

        // Create a download icon.
        val downloadIcon = ImageView(this).apply {
            setImageResource(R.drawable.ic_download)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp)
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                startActivity(intent)
            }
        }

        // Add views to the card layout.
        cardLayout.addView(iconView)
        cardLayout.addView(textView)
        cardLayout.addView(spacer)
        cardLayout.addView(downloadIcon)
        // Add the card layout to the card view.
        cardView.addView(cardLayout)
        // Add the card view to the resources container.
        resourcesContainer.addView(cardView)
    }

    // Extension to convert dp to px
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
