package com.example.studentmanagement.presentation.student_detail

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
import com.example.studentmanagement.data.dto.StudentResponse
import com.example.studentmanagement.databinding.FragmentStudentDetailScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentDetailScreen : Fragment(R.layout.fragment_student_detail_screen) {

    private val viewModel: StudentDetailViewModel by viewModel()

    private var _binding: FragmentStudentDetailScreenBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentDetailScreenBinding.bind(view)

        val backBtn = view.findViewById<ImageView>(R.id.goBack)
        val studentDetail = view.findViewById<TextView>(R.id.toolbarTitle)

        studentDetail.text = "Student Detail"
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        val student: StudentResponse? = arguments?.getParcelable("student")
        if (student != null) {
            viewModel.onAction(StudentDetailAction.LoadStudent(student))
        } else {
            viewModel.onAction(StudentDetailAction.LoadCurrentStudent)
        }

        binding.imgStudent.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val currentStudent = viewModel.uiState.value.student

            val updatedStudent = StudentResponse(
                name = currentStudent?.name,
                studentID = currentStudent?.studentID,
                imageUrl = currentStudent?.imageUrl,
                email = binding.edtEmail.text.toString(),
                address = binding.edtAddress.text.toString(),
                phone = binding.edtPhone.text.toString(),
                age = binding.edtAge.text.toString().toIntOrNull(),
                guardian = binding.edtGuardian.text.toString(),
                guardianContact = binding.edtGuardianContact.text.toString(),
                majoring = binding.edtMajoring.text.toString(),
                authUid = currentStudent?.authUid
            )

            viewModel.onAction(StudentDetailAction.SaveStudent(updatedStudent))
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    state.student?.let { student ->
                        binding.apply {
                            studentDetailName.text = student.name ?: "N/A"
                            studentDetailID.text = "ID : ${student.studentID ?: "N/A"}"
                            edtEmail.setText(student.email ?: "")
                            edtAddress.setText(student.address ?: "")
                            edtPhone.setText(student.phone ?: "")
                            edtAge.setText(student.age?.toString() ?: "")
                            edtGuardian.setText(student.guardian ?: "")
                            edtGuardianContact.setText(student.guardianContact ?: "")
                            edtMajoring.setText(student.majoring ?: "")

                            if (!student.imageUrl.isNullOrEmpty()) {
                                Glide.with(this@StudentDetailScreen)
                                    .load(student.imageUrl)
                                    .placeholder(R.drawable.ic_place_holder_profile)
                                    .into(imgStudent)
                            } else {
                                imgStudent.setImageResource(R.drawable.ic_place_holder_profile)
                            }
                        }
                    }

                    binding.progressBar.isVisible = state.isLoading

                    state.error?.let { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        viewModel.onAction(StudentDetailAction.ClearError)
                    }
                }
            }
        }
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.onAction(StudentDetailAction.UploadImage(it))
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}