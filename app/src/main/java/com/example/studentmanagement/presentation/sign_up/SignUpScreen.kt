package com.example.studentmanagement.presentation.sign_up

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.resources.StringRes
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignUpFragment : Fragment(R.layout.activity_sign_up_screen) {
    private val viewModel: SignUpViewModel by viewModel()
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signupBtn: Button
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var backButton: ImageView
    private lateinit var genderRadioGroup: RadioGroup

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailInput = view.findViewById(R.id.signupEmail)
        passwordInput = view.findViewById(R.id.signupPassword)
        signupBtn = view.findViewById(R.id.btnSignup)
        errorText = view.findViewById(R.id.errorText)
        progressBar = view.findViewById(R.id.progressBar)
        titleText = view.findViewById(R.id.signupTitle)
        backButton = view.findViewById(R.id.goBack)
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)

        titleText.text = "Teacher Sign Up"
        genderRadioGroup.visibility = View.VISIBLE
        signupBtn.text = "Register as Teacher"

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        signupBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val gender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.maleRadioButton -> "Male"
                R.id.femaleRadioButton -> "Female"
                else -> ""
            }
            if (validateTeacherInputs(email, password, gender)) {
                viewModel.onAction(SignUpAction.SubmitTeacher(email, password, gender))
            }
        }

        observeState()
    }

    private fun validateTeacherInputs(email: String, password: String, gender: String): Boolean {
        return when {
            email.isEmpty() -> showError(StringRes.EMAIL_CAN_NOT_BE_EMPTY)
            password.isEmpty() -> showError("Password cannot be empty")
            gender.isEmpty() -> showError("Please select gender")
            else -> true
        }
    }

    private fun showError(message: String): Boolean {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        return false
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    signupBtn.isEnabled = !state.isLoading

                    state.error?.let {
                        errorText.text = it
                        errorText.visibility = View.VISIBLE
                    }

                    if (state.success) {
                        findNavController().navigate(
                            R.id.action_signUp_to_login,
                            bundleOf("accountType" to "teacher"),
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