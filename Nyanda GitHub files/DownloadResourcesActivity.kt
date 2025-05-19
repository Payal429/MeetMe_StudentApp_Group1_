package com.example.studentappointmentscheduler

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.Search
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.json.JSONObject

class DownloadResourcesActivity : AppCompatActivity() {

    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var resourcesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_resources)

        moduleDropdown = findViewById(R.id.moduleDropdown)
        resourcesContainer = findViewById(R.id.resourcesContainer)

        setupModuleDropdown()
    }

    private fun setupModuleDropdown() {
        val modules = listOf("Module A", "Module B", "Module C")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)
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

                runOnUiThread {
                    if (resources.length() == 0) {
                        Toast.makeText(this, "No files found.", Toast.LENGTH_SHORT).show()
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
                runOnUiThread {
                    Toast.makeText(this, "Error fetching files: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun addResourceCard(fileName: String, fileUrl: String) {
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

        val cardLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
        }

        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.ic_document)
            layoutParams = LinearLayout.LayoutParams(24.dp, 24.dp).apply {
                marginEnd = 16
            }
        }

        val textView = TextView(this).apply {
            text = fileName
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                weight = 1f
            }
        }

        val downloadIcon = ImageView(this).apply {
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

    // Extension to convert dp to px
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}