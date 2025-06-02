package com.example.studentmanagement.presentation.sign_up

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignUpFragment : Fragment(R.layout.activity_sign_up_screen) {
    private val viewModel: SignUpViewModel by viewModel()
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var signupBtn: Button
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var backButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        emailInput = view.findViewById(R.id.signupEmail)
        passwordInput = view.findViewById(R.id.signupPassword)
        nameInput = view.findViewById(R.id.signupName)
        phoneInput = view.findViewById(R.id.signupPhone)
        signupBtn = view.findViewById(R.id.btnSignup)
        errorText = view.findViewById(R.id.errorText)
        progressBar = view.findViewById(R.id.progressBar)
        titleText = view.findViewById(R.id.signupTitle)
        backButton = view.findViewById(R.id.goBack)

        val accountType = arguments?.getString("accountType") ?: "teacher"

        // Setup UI based on account type
        if (accountType == "student") {
            titleText.text = "Student Sign Up"
            nameInput.visibility = View.VISIBLE
            phoneInput.visibility = View.VISIBLE
            signupBtn.text = "Register as Student"
        } else {
            titleText.text = "Teacher Sign Up"
            nameInput.visibility = View.GONE
            phoneInput.visibility = View.GONE
            signupBtn.text = "Register as Teacher"
        }

        // Back button navigation
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Sign up button click
        signupBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (accountType == "student") {
                val name = nameInput.text.toString()
                val phone = phoneInput.text.toString()
                if (validateStudentInputs(email, password, name, phone)) {
                    viewModel.onAction(SignUpAction.SubmitStudent(email, password, name, phone))
                }
            } else {
                if (validateTeacherInputs(email, password)) {
                    viewModel.onAction(SignUpAction.SubmitTeacher(email, password))
                }
            }
        }

        // Observe state
        observeState(accountType)
    }

    private fun validateStudentInputs(
        email: String,
        password: String,
        name: String,
        phone: String
    ): Boolean {
        return when {
            email.isEmpty() -> showError("Email cannot be empty")
            password.isEmpty() -> showError("Password cannot be empty")
            name.isEmpty() -> showError("Name cannot be empty")
            phone.isEmpty() -> showError("Phone cannot be empty")
            else -> true
        }
    }

    private fun validateTeacherInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> showError("Email cannot be empty")
            password.isEmpty() -> showError("Password cannot be empty")
            else -> true
        }
    }

    private fun showError(message: String): Boolean {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        return false
    }

    private fun observeState(accountType: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    signupBtn.isEnabled = !state.isLoading

                    state.error?.let {
                        errorText.text = it
                        errorText.visibility = View.VISIBLE
//                        viewModel.errorShown()
                    }

                    if (state.success) {
                        findNavController().navigate(
                            R.id.action_signUp_to_login,
                            bundleOf("accountType" to accountType),
                            NavOptions.Builder()
                                .setPopUpTo(R.id.action_signUp_to_login, false)
                                .build()
                        )
                    }
                }
            }
        }
    }
}


