package com.example.studentmanagement.presentation.sign_up

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


sealed class SignUpAction {
    data class SubmitTeacher(
        val email: String,
        val password: String,
        val gender: String,
        val username: String
    ) : SignUpAction()

    data object ClearError : SignUpAction()
    data object ResetState : SignUpAction()
}
data class SignUpUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)