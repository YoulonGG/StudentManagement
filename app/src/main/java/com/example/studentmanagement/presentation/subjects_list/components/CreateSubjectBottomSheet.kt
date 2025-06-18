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
                binding.subjectImage.setImageURI(it)
                Log.e("TAG", "Selected image URI: $it")
                binding.textAddImage.isVisible = false
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
    }

    private fun setupImagePicker() {
        binding.imageContainer.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun setupCreateButton() {
        binding.buttonCreate.setOnClickListener {
            val name = binding.subjectNameInput.text.toString().trim()
            val description = binding.subjectDescriptionInput.text.toString().trim()

            if (name.isEmpty()) {
                binding.subjectNameLayout.error = "Subject name cannot be empty"
            } else {
                onSubjectCreated?.invoke(name, description, imageUri)
                dismiss()
            }
        }
    }
}
