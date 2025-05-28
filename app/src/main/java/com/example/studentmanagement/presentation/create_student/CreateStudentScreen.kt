package com.example.studentmanagement.presentation.create_student

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studentmanagement.R
import com.example.studentmanagement.data.dto.request.CreateStudentDataRequest

class CreateStudentFragment : Fragment(R.layout.activity_create_student) {

    private lateinit var viewModel: CreateStudentViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        view.findViewById<View>(R.id.main).requestApplyInsets()

        viewModel = ViewModelProvider(this)[CreateStudentViewModel::class.java]

        val fullName = view.findViewById<EditText>(R.id.editTextFullName)
        val email = view.findViewById<EditText>(R.id.editTextEmail)
        val address = view.findViewById<EditText>(R.id.editTextAddress)
        val phone = view.findViewById<EditText>(R.id.editTextPhone)
        val age = view.findViewById<EditText>(R.id.editTextAge)
        val studentId = view.findViewById<EditText>(R.id.editTextStudentId)
        val guardian = view.findViewById<EditText>(R.id.editTextGuardian)
        val guardianContact = view.findViewById<EditText>(R.id.editTextGuardianContact)
        val majoring = view.findViewById<EditText>(R.id.editTextMajoring)
        val buttonSubmit = view.findViewById<Button>(R.id.buttonSubmitStudent)

        buttonSubmit.setOnClickListener {
            val ageInt = age.text.toString().toIntOrNull() ?: 0
            val intent = CreateStudentAction.OnCreateStudent(
                data = CreateStudentDataRequest(
                    fullName = fullName.text.toString(),
                    email = email.text.toString(),
                    address = address.text.toString(),
                    phone = phone.text.toString(),
                    age = ageInt,
                    studentId = studentId.text.toString(),
                    guardian = guardian.text.toString(),
                    guardianContact = guardianContact.text.toString(),
                    majoring = majoring.text.toString()
                )
            )
            viewModel.handleIntent(intent)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.isLoading -> {
                    Toast.makeText(requireContext(), "Creating student...", Toast.LENGTH_SHORT)
                        .show()
                }

                state.isSuccess -> {
                    Toast.makeText(
                        requireContext(),
                        "Student created successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

                state.errorMessage != null -> {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${state.errorMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
