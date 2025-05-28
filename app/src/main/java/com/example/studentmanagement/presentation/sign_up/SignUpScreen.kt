package com.example.studentmanagement.presentation.sign_up

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R

class SignUpFragment : Fragment(R.layout.activity_sign_up_screen) {

    private val viewModel: SignUpViewModel by viewModels()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var signupBtn: Button
    private lateinit var errorText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startIntentCollector()

        emailInput = view.findViewById(R.id.signupEmail)
        passwordInput = view.findViewById(R.id.signupPassword)
        signupBtn = view.findViewById(R.id.btnSignup)
        errorText = view.findViewById(R.id.errorText)
        progressBar = view.findViewById(R.id.progressBar)

        signupBtn.setOnClickListener {
            errorText.visibility = View.GONE
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            viewModel.sendIntent(SignUpAction.Submit(email, password))
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is SignUpState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        errorText.visibility = View.GONE
                    }

                    is SignUpState.Success -> {
                        progressBar.visibility = View.GONE
                        findNavController().navigate(
                            R.id.action_signUp_to_login,
                            bundleOf("accountType" to "teacher")
                        )
                    }

                    is SignUpState.Error -> {
                        progressBar.visibility = View.GONE
                        errorText.text = state.message
                        errorText.visibility = View.VISIBLE
                    }

                    else -> {
                        progressBar.visibility = View.GONE
                        errorText.visibility = View.GONE
                    }
                }
            }
        }
    }
}




