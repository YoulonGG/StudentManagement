package com.example.studentmanagement.presentation.login

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel(
    private val auth: FirebaseAuth,
) : BaseViewModel<LoginAction, LoginUiState>() {

    override fun setInitialState(): LoginUiState = LoginUiState()

    override fun onAction(event: LoginAction) {
        when (event) {
            is LoginAction.Login -> login(event.email, event.password, event.accountType)
        }
    }

    private fun login(email: String, password: String, accountType: String) {
        setState { copy(isLoading = true, error = null) }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            accountType = accountType
                        )
                    }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = task.exception?.message
                                ?: "Login failed. Check credentials and try again."
                        )
                    }
                }
            }
    }

    fun errorShown() {
        setState { copy(error = null) }
    }
}

