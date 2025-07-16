package com.example.studentmanagement.presentation.login

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

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