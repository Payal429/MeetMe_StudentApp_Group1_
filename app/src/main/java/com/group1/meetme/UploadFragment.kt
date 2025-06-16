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
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private var selectedFileUri: Uri? = null

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
        fun newInstance() = UploadFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        loadLanguage()
    }

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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                binding.uploadFileText.text = "Selected File: ${uri.lastPathSegment}"
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadToCloudinary(fileUri: Uri) {
        val selectedModule = binding.moduleDropdown.text.toString()

        if (selectedModule.isBlank()) {
            Toast.makeText(requireContext(), "Please select a module.", Toast.LENGTH_SHORT).show()
            return
        }

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

    private fun loadLanguage() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", 0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}