package com.example.studentmanagement.presentation.create_student

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateStudentFragment : Fragment(R.layout.fragment_create_student) {
    private val viewModel: CreateStudentViewModel by viewModel()

    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var studentIdInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var createBtn: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView
    private lateinit var toolbarTitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput = view.findViewById(R.id.createStudentName)
        studentIdInput = view.findViewById(R.id.createStudent)
        emailInput = view.findViewById(R.id.createStudentEmail)
        genderSpinner = view.findViewById(R.id.genderSpinner)
        createBtn = view.findViewById(R.id.btnCreateStudent)
        errorText = view.findViewById(R.id.createStudentErrorText)
        progressBar = view.findViewById(R.id.createStudentProgressBar)
        backButton = view.findViewById(R.id.goBack)
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        toolbarTitle.text = getString(R.string.create_student)

        setupGenderSelection()
        backButton.setOnClickListener { findNavController().navigateUp() }

        createBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val email = emailInput.text.toString().trim()
            val name = nameInput.text.toString().trim()
            val studentID = studentIdInput.text.toString().trim()
            val gender = if (genderSpinner.selectedItemPosition != 0)
                genderSpinner.selectedItem.toString()
            else ""

            viewModel.onAction(CreateStudentAction.SubmitStudent(email, name, studentID, gender))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                createBtn.isEnabled = !state.isLoading

                if (state.error != null) {
                    when (state.error) {
                        "DUPLICATE_STUDENT_ID" -> {
                            Dialog.showDialog(
                                requireContext(),
                                title = "Duplicate Student ID",
                                description = "Student ID '${state.duplicateStudentId}' is already taken. Please use a different ID."
                            ) {
                                studentIdInput.text.clear()
                                studentIdInput.requestFocus()
                            }
                            viewModel.clearError()
                        }

                        else -> {
                            errorText.text = state.error
                            errorText.visibility = View.VISIBLE
                        }
                    }
                }
                if (state.success) {
                    Dialog.showDialog(
                        requireContext(),
                        title = "Success",
                        description = "Student created successfully!"
                    ) {
                        findNavController().navigate(
                            R.id.navigate_create_student_to_teacher,
                            bundleOf("accountType" to "teacher"),
                            NavOptions.Builder()
                                .setPopUpTo(R.id.navigate_create_student_to_teacher, false)
                                .build()
                        )
                    }
                    viewModel.resetState()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupGenderSelection() {
        val genders = arrayOf("Select Gender", "Male", "Female")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            genders
        ).apply {
            setDropDownViewResource(R.layout.spinner_item)
        }

        genderSpinner.adapter = adapter

        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as? TextView)?.let { textView ->
                    val color = if (position == 0)
                        ContextCompat.getColor(requireContext(), R.color.hint_color)
                    else
                        ContextCompat.getColor(requireContext(), R.color.primary)
                    textView.setTextColor(color)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
