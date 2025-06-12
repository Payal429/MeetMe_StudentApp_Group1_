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

// Activity for uploading resources.
class UploadResourcesActivity : AppCompatActivity() {

    private lateinit var courseDropdown: MaterialAutoCompleteTextView
    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnUpload: Button
    private lateinit var uploadFileText: TextView
    private lateinit var paperclipIcon: ImageView
    private lateinit var backButton: ImageView

    private var selectedFileUri: Uri? = null

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_resources)

        // Initialize UI components.
        courseDropdown = findViewById(R.id.courseDropdown)
        moduleDropdown = findViewById(R.id.moduleDropdown)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        uploadFileText = findViewById(R.id.uploadFileText)
        paperclipIcon = findViewById(R.id.paperclipIcon)
        backButton = findViewById(R.id.backArrow)

        // Set up dropdowns for courses and modules.
        setupDropdowns()

        // Go back to menu when back button is clicked
        backButton.setOnClickListener {
            val intent = Intent(this, MenuResourcesActivity::class.java)
            startActivity(intent)
        }

        // Open file picker when the paperclip icon or select file button is clicked.
        paperclipIcon.setOnClickListener { openFilePicker() }
        btnSelectFile.setOnClickListener { openFilePicker() }

        // Upload the selected file to Cloudinary when the upload button is clicked.
        btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadToCloudinary(uri)
            } ?: Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
        }

        // Load saved language preference
        loadLanguage()

    }

    private fun loadLanguage() {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Function to set up the dropdowns for courses and modules.
    private fun setupDropdowns() {
        val courses = listOf("IT", "Law", "Education", "Commerce", "Policing and Law Enforcement", "Administration and Management", "Fashion")
        val modules = listOf("Information Systems", "Web Development Project", "Mobile Application Development A", "Commercial Law", "Project Management", "Introduction to Research")

        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courses)
        val moduleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)

        courseDropdown.setAdapter(courseAdapter)
        moduleDropdown.setAdapter(moduleAdapter)
    }

    // Function to open the file picker
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE)
    }

    // Handle the result of the file picker.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
            }
        }
    }

    // Function to upload the selected file to Cloudinary.
    private fun uploadToCloudinary(fileUri: Uri) {
        val selectedModule = moduleDropdown.text.toString()

        if (selectedModule.isBlank()) {
            Toast.makeText(this, "Please select a module.", Toast.LENGTH_SHORT).show()
            return
        }

        MediaManager.get().upload(fileUri)
            .option("tags", listOf(selectedModule))
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(applicationContext, "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Toast.makeText(applicationContext, "Upload Successful!", Toast.LENGTH_LONG).show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(applicationContext, "Upload Failed: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}