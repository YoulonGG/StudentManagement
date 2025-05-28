package com.example.studentmanagement.presentation.sign_up

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */


sealed class SignUpAction {
    data class Submit(val email: String, val password: String) : SignUpAction()
}

sealed class SignUpState {
    data object Idle : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}