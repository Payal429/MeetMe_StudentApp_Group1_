package com.group1.meetme

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import java.util.Locale

// Activity that allows users to upload resources (like files) associated with modules
class UploadResourcesActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var courseDropdown: MaterialAutoCompleteTextView
    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnUpload: Button
    private lateinit var uploadFileText: TextView
    private lateinit var paperclipIcon: ImageView
    private lateinit var backButton: ImageView

    // Store the selected file's URI
    private var selectedFileUri: Uri? = null

    // Constant request code for the file picker intent
    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout for this activity
        setContentView(R.layout.activity_upload_resources)

        // Initialize UI components by their IDs
        courseDropdown = findViewById(R.id.courseDropdown)
        moduleDropdown = findViewById(R.id.moduleDropdown)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        uploadFileText = findViewById(R.id.uploadFileText)
        paperclipIcon = findViewById(R.id.paperclipIcon)
        backButton = findViewById(R.id.backArrow)

        // Populate the dropdowns with course and module options
        setupDropdowns()

        // Set up navigation back to the main menu
        backButton.setOnClickListener {
            val intent = Intent(this, MenuResourcesActivity::class.java)
            startActivity(intent)
        }

        // Launch the file picker when either the icon or button is clicked
        paperclipIcon.setOnClickListener { openFilePicker() }
        btnSelectFile.setOnClickListener { openFilePicker() }

        // Upload file to Cloudinary when upload button is clicked
        btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadToCloudinary(uri)
            } ?: Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
        }

        // Load and apply the user's language preference
        loadLanguage()
    }

    // Load the preferred language from SharedPreferences and apply it
    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Update the app's locale (language) based on the given language code
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = resources.configuration
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Initialize the course and module dropdown menus
    private fun setupDropdowns() {
        val courses = listOf(
            "IT", "Law", "Education", "Commerce",
            "Policing and Law Enforcement", "Administration and Management", "Fashion"
        )

        val modules = listOf(
            "Information Systems", "Web Development Project",
            "Mobile Application Development A", "Commercial Law",
            "Project Management", "Introduction to Research"
        )

        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courses)
        val moduleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)

        courseDropdown.setAdapter(courseAdapter)
        moduleDropdown.setAdapter(moduleAdapter)
    }

    // Launch the system file picker to select a file
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"  // Accept any file type
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE)
    }

    // Handle the result from the file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                // Display the selected file's name
                uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
            }
        }
    }

    // Upload the selected file to Cloudinary with the selected module as a tag
    private fun uploadToCloudinary(fileUri: Uri) {
        val selectedModule = moduleDropdown.text.toString()

        // Ensure that a module has been selected
        if (selectedModule.isBlank()) {
            Toast.makeText(this, "Please select a module.", Toast.LENGTH_SHORT).show()
            return
        }

        // Begin the upload using Cloudinary's MediaManager
        MediaManager.get().upload(fileUri)
            .option("tags", listOf(selectedModule)) // Tag the upload with module name
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(applicationContext, "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    // Optional: Update progress UI here
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Toast.makeText(applicationContext, "Upload Successful!", Toast.LENGTH_LONG).show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(applicationContext, "Upload Failed: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    // Optional: Handle retry logic here
                }
            })
            .dispatch()
    }
}
