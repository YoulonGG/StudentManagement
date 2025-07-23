package com.example.studentmanagement.presentation.sign_up

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.resources.StringRes
import com.example.studentmanagement.core.utils.animateNav
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignUpFragment : Fragment(R.layout.activity_sign_up_screen) {
    private val viewModel: SignUpViewModel by viewModel()
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signupBtn: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView
    private lateinit var genderSpinner: Spinner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameInput = view.findViewById(R.id.signupUsername)
        emailInput = view.findViewById(R.id.signupEmail)
        passwordInput = view.findViewById(R.id.signupPassword)
        signupBtn = view.findViewById(R.id.btnSignup)
        errorText = view.findViewById(R.id.errorText)
        progressBar = view.findViewById(R.id.progressBar)
        backButton = view.findViewById(R.id.goBack)
        genderSpinner = view.findViewById(R.id.genderSpinner)

        genderSelection()
        backButton.setOnClickListener { findNavController().popBackStack() }

        signupBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val gender = genderSpinner.selectedItem.toString()

            if (validateTeacherInputs(username, email, password, gender)) {
                viewModel.onAction(SignUpAction.SubmitTeacher(email, password, gender, username))
            }
        }

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
                            animateNav()
//                            NavOptions.Builder()
//                                .setPopUpTo(R.id.action_signUp_to_login, false)
//                                .build()
                        )
                    }
                }
            }
        }
    }

    private fun genderSelection() {
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
                    if (position == 0) {
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.hint_color
                            )
                        )
                    } else {
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary
                            )
                        )
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun validateTeacherInputs(
        username: String,
        email: String,
        password: String,
        gender: String
    ): Boolean {
        return when {
            username.isEmpty() -> showError("Username cannot be empty")
            email.isEmpty() -> showError(StringRes.EMAIL_CAN_NOT_BE_EMPTY)
            password.isEmpty() -> showError("Password cannot be empty")
            gender == "Select Gender" -> showError("Please select gender")
            else -> true
        }
    }

    private fun showError(message: String): Boolean {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        return false
    }

}