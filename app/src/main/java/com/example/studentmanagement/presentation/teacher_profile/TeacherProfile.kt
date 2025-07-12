package com.example.studentmanagement.presentation.teacher_profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.data.dto.TeacherResponse
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.databinding.FragmentTeacherProfileBinding
import com.example.studentmanagement.presentation.activity.MainActivity
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

        val goBack = view.findViewById<ImageView>(R.id.goBack)
        val logOut = view.findViewById<TextView>(R.id.toolbar_logout_btn)
        val teacherProfileToolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)

        teacherProfileToolbarTitle.text = "My Profile"
        logOut.text = "Log Out"

        logOut.setOnClickListener {
//            Dialog.showDialog(
//                requireContext(),
//                "Log Out",
//                "Are you sure you want to log out?",
//                positiveButtonText = "Yes",
//                negativeButtonText = "No",
//                onPositiveClick = {
//                    Logout()
//                },
//                onNegativeClick = {}
//            )
            Logout()
        }

        goBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.onAction(TeacherProfileAction.LoadCurrentTeacher)

        binding.teacherDetailImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val teacher = TeacherResponse(
                accountType = "teacher",
                username = binding.teacherDetailName.text.toString(),
                email = binding.teacherDetailEmail.text.toString(),
                address = binding.teacherDetailAddress.text.toString(),
                gender = binding.teacherDetailGender.text.toString(),
                phone = binding.teacherDetailPhoneNumber.text.toString(),
                imageUrl = viewModel.uiState.value.teacher?.imageUrl
            )
            viewModel.onAction(TeacherProfileAction.SaveTeacher(teacher))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.teacher != null) {
                        binding.teacherDetailName.setText(state.teacher.username ?: "")
                        binding.teacherDetailEmail.setText(state.teacher.email ?: "")
                        binding.teacherDetailPhoneNumber.setText(state.teacher.phone ?: "")
                        binding.teacherDetailAddress.setText(state.teacher.address ?: "")
                        binding.teacherDetailAge.setText(state.teacher.age ?: "")
                        binding.teacherDetailGender.setText(state.teacher.gender ?: "")

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

    private fun Logout() {
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(PreferencesKeys.IS_LOGGED_IN)
            remove(PreferencesKeys.ACCOUNT_TYPE)
            apply()
        }

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}