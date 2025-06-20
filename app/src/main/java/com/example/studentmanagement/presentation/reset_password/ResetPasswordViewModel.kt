package com.example.studentmanagement.presentation.reset_password

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * @Author: John Youlong.
 * @Date: 6/18/25.
 * @Email: johnyoulong@gmail.com.
 */


class ResetPasswordViewModel(
    private val auth: FirebaseAuth
) : BaseViewModel<PasswordResetAction, PasswordResetUiState>() {

    override fun setInitialState(): PasswordResetUiState = PasswordResetUiState()

    override fun onAction(event: PasswordResetAction) {
        when (event) {
            is PasswordResetAction.SendResetEmail -> handlePasswordReset(event.email)
        }
    }

    private fun handlePasswordReset(email: String) {
        setState { copy(isLoading = true, error = null) }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                setState { copy(isLoading = false, success = true) }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to send reset email: ${e.message}"
                    )
                }
            }
    }
}

sealed class PasswordResetAction {
    data class SendResetEmail(val email: String) : PasswordResetAction()
}

data class PasswordResetUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)