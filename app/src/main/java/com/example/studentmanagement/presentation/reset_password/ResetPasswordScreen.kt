package com.example.studentmanagement.presentation.reset_password

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ResetPasswordScreen : Fragment(R.layout.fragment_reset_password_screen) {
    private val viewModel: ResetPasswordViewModel by viewModel()
    private lateinit var emailInput: EditText
    private lateinit var resetButton: TextView
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailInput = view.findViewById(R.id.resetEmailInput)
        resetButton = view.findViewById(R.id.resetButton)
        errorText = view.findViewById(R.id.errorText)
        progressBar = view.findViewById(R.id.progressBar)
        backButton = view.findViewById(R.id.goBack)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        resetButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (validateEmail(email)) {
                viewModel.onAction(PasswordResetAction.SendResetEmail(email))
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    resetButton.isEnabled = !state.isLoading

                    state.error?.let {
                        errorText.text = it
                        errorText.visibility = View.VISIBLE
                    }

                    if (state.success) {
                        Dialog.showDialog(
                            requireContext(),
                            title = "Check your Email",
                            description = "Reset link sent successfully!"
                        ) {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                errorText.text = "Email can not be empty"
                errorText.visibility = View.VISIBLE
                false
            }

            else -> true
        }
    }
}