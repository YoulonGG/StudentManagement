package com.example.studentmanagement.presentation.login

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.example.studentmanagement.data.local.PreferencesKeys
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : Fragment(R.layout.activity_login_screen) {
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginBtn = view.findViewById<Button>(R.id.btnLogin)
        val emailEt = view.findViewById<EditText>(R.id.loginEmail)
        val passEt = view.findViewById<EditText>(R.id.loginPassword)
        val title = view.findViewById<TextView>(R.id.loginTitle)
        val signupText = view.findViewById<TextView>(R.id.tvGoSignup)
        val backButton = view.findViewById<ImageView>(R.id.goBack)

        val accountType = arguments?.getString("accountType") ?: "student"

        title.text = "Login as ${accountType.replaceFirstChar { it.uppercase() }}"
        signupText.text = if (accountType == "student") "Don't have an account? Sign Up as Student"
        else "Don't have an account? Sign Up as Teacher"

        signupText.setOnClickListener {
            findNavController().navigate(
                R.id.navigate_login_to_signUp,
                bundleOf("accountType" to accountType)
            )
        }

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

//        backButton.setOnClickListener {
//            if (findNavController().previousBackStackEntry != null) {
//                findNavController().navigateUp()
//            } else {
//                findNavController().navigateUp()
//            }
//        }

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please fill in all fields")
            } else {
                loginViewModel.onAction(LoginAction.Login(email, password, accountType))
            }
        }

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.uiState.collect { state ->
                    handleLoginState(state, accountType)
                }
            }
        }
    }

    private fun handleLoginState(state: LoginUiState, accountType: String) {
        val loginBtn = requireView().findViewById<Button>(R.id.btnLogin)

        loginBtn.isEnabled = !state.isLoading
        loginBtn.text = if (state.isLoading) "Logging in..." else "Login"

        state.error?.let {
            showToast(it)
            loginViewModel.errorShown()
        }

        if (state.success) {
            saveLoginAndNavigate(state.accountType ?: accountType)
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







