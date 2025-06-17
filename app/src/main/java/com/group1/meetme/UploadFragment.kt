package com.group1.meetme

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.group1.meetme.databinding.FragmentUploadBinding
import java.util.Locale

class UploadFragment : Fragment() {
    // View binding for fragment layout
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    // Store the selected file URI for upload
    private var selectedFileUri: Uri? = null

    // Constant for identifying file picker result
    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
        // Factory method for creating a new instance of this fragment
        fun newInstance() = UploadFragment()
    }

    // Inflate the fragment layout and initialize view binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Called after the view has been created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate course and module dropdowns
        setupDropdowns()

        // Open file picker
        binding.paperclipIcon.setOnClickListener { openFilePicker() }
        binding.btnSelectFile.setOnClickListener { openFilePicker() }

        // Upload file
        binding.btnUpload.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadToCloudinary(uri)
            } ?: Toast.makeText(requireContext(), "Please select a file first.", Toast.LENGTH_SHORT).show()
        }

        // Load language preferences for localization
        loadLanguage()
    }

    // Function to initialize dropdown menus for course and module selections
    private fun setupDropdowns() {
        val courses = listOf(
            "IT", "Law", "Education", "Commerce",
            "Policing and Law Enforcement", "Administration and Management", "Fashion"
        )
        val modules = listOf(
            "Information Systems", "Web Development Project", "Mobile Application Development A",
            "Commercial Law", "Project Management", "Introduction to Research"
        )

        val courseAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, courses)
        val moduleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modules)

        binding.courseDropdown.setAdapter(courseAdapter)
        binding.moduleDropdown.setAdapter(moduleAdapter)
    }

    // Launch the file picker to select a file from device
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE)
    }

    // Handle the result returned from file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                binding.uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Upload the selected file to Cloudinary with selected module as a tag
    private fun uploadToCloudinary(fileUri: Uri) {
        val selectedModule = binding.moduleDropdown.text.toString()

        if (selectedModule.isBlank()) {
            Toast.makeText(requireContext(), "Please select a module.", Toast.LENGTH_SHORT).show()
            return
        }

        // Use Cloudinary MediaManager to handle the upload
        MediaManager.get().upload(fileUri)
            .option("tags", listOf(selectedModule))
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    Toast.makeText(requireContext(), "Upload Successful!", Toast.LENGTH_LONG).show()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Upload Failed: ${error?.description}", Toast.LENGTH_LONG).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    // Load the user's language preference and apply locale
    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", 0)
        val savedLanguage = sharedPref.getString("language", "en")  // Default to English
        setLocale(savedLanguage ?: "en")
    }

    // Set the locale for the fragment
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    // Clear the binding when view is destroyed to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}