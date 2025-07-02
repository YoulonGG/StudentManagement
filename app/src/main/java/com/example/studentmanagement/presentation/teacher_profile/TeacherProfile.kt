package com.example.studentmanagement.presentation.teacher_profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.data.dto.TeacherResponse
import com.example.studentmanagement.databinding.FragmentTeacherProfileBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TeacherProfile : Fragment(R.layout.fragment_teacher_profile) {

    private var _binding: FragmentTeacherProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeacherProfileViewModel by viewModel()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.onAction(TeacherProfileAction.UploadImage(it))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTeacherProfileBinding.bind(view)

        val goBack = view.findViewById<View>(R.id.goBack)
        goBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.onAction(TeacherProfileAction.LoadCurrentTeacher)

        binding.teacherDetailImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val teacher = TeacherResponse(
                username = binding.teacherDetailName.text.toString(),
                email = binding.teacherDetailEmail.text.toString(),
                phone = binding.teacherDetailPhoneNumber.text.toString(),
                imageUrl = viewModel.uiState.value.teacher?.imageUrl
            )
            viewModel.onAction(TeacherProfileAction.SaveTeacher(teacher))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.teacher != null) {
                        binding.teacherDetailName.setText(state.teacher.username)
                        binding.teacherDetailEmail.setText(state.teacher.email)
                        binding.teacherDetailPhoneNumber.setText(state.teacher.phone)

                        if (!state.teacher.imageUrl.isNullOrEmpty()) {
                            Glide.with(this@TeacherProfile)
                                .load(state.teacher.imageUrl)
                                .placeholder(R.drawable.ic_place_holder_profile)
                                .into(binding.teacherDetailImage)
                        } else {
                            binding.teacherDetailImage.setImageResource(R.drawable.ic_place_holder_profile)
                        }
                    }

                    binding.progressBar.isVisible = state.isLoading

                    state.error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}