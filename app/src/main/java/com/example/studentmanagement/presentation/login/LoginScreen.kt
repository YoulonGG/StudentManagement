package com.example.studentmanagement.presentation.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.data.local.PreferencesKeys
import java.util.Locale

class LoginFragment : Fragment(R.layout.activity_login_screen) {

    private lateinit var viewModel: LoginViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = LoginViewModel()
        viewModel.start()

        val loginBtn = view.findViewById<Button>(R.id.btnLogin)
        val emailEt = view.findViewById<EditText>(R.id.loginEmail)
        val passEt = view.findViewById<EditText>(R.id.loginPassword)
        val title = view.findViewById<TextView>(R.id.loginTitle)
        val signup = view.findViewById<TextView>(R.id.tvGoSignup)

        val accountType = arguments?.getString("accountType") ?: "student"

        title.text = "Login as ${
            accountType.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
        }"

        if (accountType == "student") {
            signup.visibility = View.GONE
        }

        signup.setOnClickListener {
            findNavController().navigate(R.id.navigate_login_to_signUp)
        }

        val backButton = view.findViewById<ImageView>(R.id.goBack)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }


        loginBtn.setOnClickListener {
            val email = emailEt.text.toString()
            val pass = passEt.text.toString()
            viewModel.sendIntent(LoginAction.Login(email, pass, accountType))
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                when (state) {
                    is LoginUiState.Idle -> Unit
                    is LoginUiState.Loading -> {
                        // Optional loading indicator
                    }

                    is LoginUiState.Success -> {

                        val sharedPref =
                            requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putBoolean(PreferencesKeys.IS_LOGGED_IN, true)
                            putString(PreferencesKeys.ACCOUNT_TYPE, state.accountType)
                            apply()
                        }

                        val action = if (state.accountType == "teacher")
                            R.id.navigate_login_to_teacher
                        else
                            R.id.navigate_login_to_student

                        findNavController().navigate(action)
                    }

                    is LoginUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }

                    null -> Unit
                }
            }
        }
    }
}





