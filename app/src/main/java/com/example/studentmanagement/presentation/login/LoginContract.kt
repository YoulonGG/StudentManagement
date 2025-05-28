package com.example.studentmanagement.presentation.login

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */


sealed class LoginAction {
    data class Login(val email: String, val password: String, val accountType: String) :
        LoginAction()
}

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val accountType: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}