package com.example.studentmanagement.presentation.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.data.local.PreferencesKeys
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.activity_login_screen) {
    private val loginViewModel: LoginViewModel by viewModel()
    private lateinit var progressBar: ProgressBar


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginBtn = view.findViewById<TextView>(R.id.btnLogin)
        val emailEt = view.findViewById<EditText>(R.id.loginEmail)
        val passEt = view.findViewById<EditText>(R.id.loginPassword)
        val title = view.findViewById<TextView>(R.id.txtLoginTitle)
        val signupText = view.findViewById<TextView>(R.id.tvGoSignup)
        val backButton = view.findViewById<ImageView>(R.id.goBack)
        val resetPassword = view.findViewById<TextView>(R.id.txtResetPassword)
        val accountType = arguments?.getString("accountType") ?: "student"
        progressBar = view.findViewById(R.id.progressBar)


        title.text = if (accountType == "teacher") "Teacher Log In" else "Student Log In"

        signupText.visibility = if (accountType == "student") View.GONE else View.VISIBLE

        signupText.setOnClickListener {
            findNavController().navigate(
                R.id.navigate_login_to_signUp,
                bundleOf("accountType" to accountType)
            )
        }
        resetPassword.setOnClickListener {
            findNavController().navigate(
                R.id.navigate_login_to_reset_password,
                bundleOf("accountType" to accountType)
            )
        }

        backButton.setOnClickListener {
            findNavController().navigate(R.id.navigate_login_to_choose_login_type)
        }


        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please fill in all fields")
            } else {
                loginViewModel.onAction(LoginAction.Login(email, password, accountType))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.uiState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    loginBtn.isEnabled = !state.isLoading

                    state.error?.let {
                        Dialog.showDialog(
                            context = requireContext(),
                            title = "Error",
                            description = it,
                            onBtnClick = {
                                loginViewModel.errorShown()
                            }
                        )
                    }

                    if (state.success) {
                        saveLoginAndNavigate(state.accountType ?: accountType)
                    }
                }
            }
        }
    }

    private fun saveLoginAndNavigate(accountType: String) {
        requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit {
            putBoolean(PreferencesKeys.IS_LOGGED_IN, true)
            putString(PreferencesKeys.ACCOUNT_TYPE, accountType)
        }

        val destination = when (accountType) {
            "teacher" -> R.id.navigate_login_to_teacher
            else -> R.id.navigate_login_to_student
        }
        findNavController().navigate(destination)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}







