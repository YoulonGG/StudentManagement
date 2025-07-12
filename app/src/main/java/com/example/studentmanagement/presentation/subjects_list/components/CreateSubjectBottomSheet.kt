package com.example.studentmanagement.presentation.subjects_list.components

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.studentmanagement.databinding.LayoutCreateSubjectBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */
class CreateSubjectBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: LayoutCreateSubjectBottomSheetBinding
    private var imageUri: Uri? = null
    private var onSubjectCreated: ((String, String, Uri?) -> Unit)? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                showImagePreview(it)
                Log.d("CreateSubject", "Selected image URI: $it")
            }
        }

    fun setOnSubjectCreatedListener(listener: (String, String, Uri?) -> Unit) {
        onSubjectCreated = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutCreateSubjectBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImagePicker()
        setupCreateButton()
        setupCancelButton()
    }

    private fun setupImagePicker() {
        binding.imageContainer.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun showImagePreview(uri: Uri) {
        binding.apply {
            textAddImage.isVisible = false

            imagePreviewContainer.isVisible = true

            imagePreviewThumbnail.setImageURI(uri)

            val fileName = getFileName(uri)
            imageFileName.text = fileName

            buttonRemoveImage.setOnClickListener {
                removeImage()
            }
        }
    }

    private fun removeImage() {
        imageUri = null
        binding.apply {
            textAddImage.isVisible = true
            imagePreviewContainer.isVisible = false
            imagePreviewThumbnail.setImageURI(null)
            imageFileName.text = ""
        }
    }

    private fun getFileName(uri: Uri): String {
        return try {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst() && nameIndex != -1) {
                    it.getString(nameIndex)
                } else {
                    "Unknown_Image.jpg"
                }
            } ?: "Unknown_Image.jpg"
        } catch (e: Exception) {
            "Unknown_Image.jpg"
        }
    }

    private fun setupCreateButton() {
        binding.buttonCreate.setOnClickListener {
            val name = binding.subjectNameInput.text.toString().trim()
            val description = binding.subjectDescriptionInput.text.toString().trim()

            // Validate input
            if (name.isEmpty()) {
                binding.subjectNameLayout.error = "Subject name cannot be empty"
                return@setOnClickListener
            }

            // Clear any previous errors
            binding.subjectNameLayout.error = null

            // Invoke callback
            onSubjectCreated?.invoke(name, description, imageUri)
            dismiss()
        }
    }

    private fun setupCancelButton() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }
}