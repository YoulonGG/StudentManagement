package com.example.studentmanagement.presentation.login

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                    // Login failed
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

sealed class LoginAction {
    data class Login(val email: String, val password: String, val accountType: String) :
        LoginAction()
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val accountType: String? = null
)