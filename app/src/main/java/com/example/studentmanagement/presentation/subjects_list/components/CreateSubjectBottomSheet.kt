package com.example.studentmanagement.presentation.subjects_list.components

import android.net.Uri
import android.os.Bundle
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
    private var onSubjectCreated: ((SubjectData) -> Unit)? = null

    data class SubjectData(
        val name: String,
        val description: String,
        val code: String,
        val className: String,
        val classTime: String,
        val imageUri: Uri?
    )

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                showImagePreview(it)
            }
        }

    fun setOnSubjectCreatedListener(listener: (SubjectData) -> Unit) {
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
            if (validateFields()) {
                val name = binding.subjectNameInput.text.toString().trim()
                val description = binding.subjectDescriptionInput.text.toString().trim()
                val code = binding.subjectCodeInput.text.toString().trim()
                val className = binding.subjectClassNameInput.text.toString().trim()
                val classTime = binding.subjectClassTimeInput.text.toString().trim()

                val subjectData = SubjectData(
                    name = name,
                    description = description,
                    code = code,
                    className = className,
                    classTime = classTime,
                    imageUri = imageUri
                )

                onSubjectCreated?.invoke(subjectData)
                clearForm()
                dismiss()
            }
        }
    }

    private fun validateFields(): Boolean {
        clearErrors()

        val name = binding.subjectNameInput.text.toString().trim()
        val description = binding.subjectDescriptionInput.text.toString().trim()
        val code = binding.subjectCodeInput.text.toString().trim()
        val className = binding.subjectClassNameInput.text.toString().trim()
        val classTime = binding.subjectClassTimeInput.text.toString().trim()

        var isValid = true

        if (name.isEmpty()) {
            binding.subjectNameInput.error = "Subject name is required"
            isValid = false
        } else if (name.length < 2) {
            binding.subjectNameInput.error = "Subject name must be at least 2 characters"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.subjectDescriptionInput.error = "Description is required"
            isValid = false
        } else if (description.length < 10) {
            binding.subjectDescriptionInput.error = "Description must be at least 10 characters"
            isValid = false
        }

        if (code.isEmpty()) {
            binding.subjectCodeInput.error = "Subject code is required"
            isValid = false
        } else if (code.length < 3) {
            binding.subjectCodeInput.error = "Subject code must be at least 3 characters"
            isValid = false
        }

        if (className.isEmpty()) {
            binding.subjectClassNameInput.error = "Class name is required"
            isValid = false
        } else if (className.length < 2) {
            binding.subjectClassNameInput.error = "Class name must be at least 2 characters"
            isValid = false
        }

        if (classTime.isEmpty()) {
            binding.subjectClassTimeInput.error = "Class time is required"
            isValid = false
        } else if (classTime.length < 5) {
            binding.subjectClassTimeInput.error =
                "Please enter a valid class time (e.g., 9:00 AM - 10:30 AM)"
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        binding.apply {
            subjectNameInput.error = null
            subjectDescriptionInput.error = null
            subjectCodeInput.error = null
            subjectClassNameInput.error = null
            subjectClassTimeInput.error = null
        }
    }

    private fun setupCancelButton() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun clearForm() {
        binding.apply {
            subjectNameInput.text?.clear()
            subjectDescriptionInput.text?.clear()
            subjectCodeInput.text?.clear()
            subjectClassNameInput.text?.clear()
            subjectClassTimeInput.text?.clear()
        }
        removeImage()
        clearErrors()
    }
}