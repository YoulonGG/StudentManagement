package com.example.studentmanagement.presentation.create_student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studentmanagement.R
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateStudentFragment : Fragment(R.layout.fragment_create_student) {
    private val viewModel: CreateStudentViewModel by viewModel()
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var studentIdInput: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var createBtn: Button
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emailInput = view.findViewById(R.id.createStudentEmail)
        passwordInput = view.findViewById(R.id.createStudentPassword)
        nameInput = view.findViewById(R.id.createStudentName)
        studentIdInput = view.findViewById(R.id.createStudentID)
        genderRadioGroup = view.findViewById(R.id.createStudentGenderRadioGroup)
        createBtn = view.findViewById(R.id.btnCreateStudent)
        errorText = view.findViewById(R.id.createStudentErrorText)
        progressBar = view.findViewById(R.id.createStudentProgressBar)

        createBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val name = nameInput.text.toString()
            val studentID = studentIdInput.text.toString()
            val gender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.createStudentMaleRadioButton -> "Male"
                R.id.createStudentFemaleRadioButton -> "Female"
                else -> ""
            }
            viewModel.createStudent(email, password, name, studentID, gender)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collectLatest { state ->
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                createBtn.isEnabled = !state.isLoading
                if (state.error != null) {
                    errorText.text = state.error
                    errorText.visibility = View.VISIBLE
                }
                if (state.success) {
                    errorText.text = "Student created successfully!"
                    errorText.visibility = View.VISIBLE
                }
            }
        }
    }
}
