package com.group1.meetme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class upload_resources : AppCompatActivity() {
    private lateinit var courseDropdown: MaterialAutoCompleteTextView
    private lateinit var moduleDropdown: MaterialAutoCompleteTextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnUpload: Button
    private lateinit var uploadFileText: TextView
    private lateinit var paperclipIcon: ImageView

    private var selectedFileUri: Uri? = null

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_resources)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        courseDropdown = findViewById(R.id.courseDropdown)
        moduleDropdown = findViewById(R.id.moduleDropdown)
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnUpload = findViewById(R.id.btnUpload)
        uploadFileText = findViewById(R.id.uploadFileText)
        paperclipIcon = findViewById(R.id.paperclipIcon)

        setupDropdowns()

        // Launch file picker on paperclip or button click
        paperclipIcon.setOnClickListener { openFilePicker() }
        btnSelectFile.setOnClickListener { openFilePicker() }

        btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadToCloudinary(uri)
            } ?: Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDropdowns() {
        val courses = listOf("Computer Science", "Math", "Physics")
        val modules = listOf("Module A", "Module B", "Module C")

        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, courses)
        val moduleAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, modules)

        courseDropdown.setAdapter(courseAdapter)
        moduleDropdown.setAdapter(moduleAdapter)
    }

    //    private fun openFilePicker() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "*/*"
//        startActivityForResult(Intent.createChooser(intent, "Select a file"),
//            com.example.StudentAppointmentScheduler.upload_resources.Companion.FILE_PICKER_REQUEST_CODE
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == com.example.StudentAppointmentScheduler.upload_resources.Companion.FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            data?.data?.let { uri ->
//                selectedFileUri = uri
//                uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
//            }
//        }
//    }
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(
            Intent.createChooser(intent, "Select a file"),
            FILE_PICKER_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
            }
        }
    }


    private fun uploadToCloudinary(fileUri: Uri) {
        val selectedModule = moduleDropdown.text.toString()

        if (selectedModule.isBlank()) {
            Toast.makeText(this, "Please select a module.", Toast.LENGTH_SHORT).show()
            return
        }

        MediaManager.get().upload(fileUri)
            .option("tags", listOf(selectedModule)) // ✅ Add tag based on selected module
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(applicationContext, "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Toast.makeText(applicationContext, "Upload Successful!", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(
                        applicationContext,
                        "Upload Failed: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}