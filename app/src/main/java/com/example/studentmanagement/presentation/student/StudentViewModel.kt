package com.example.studentmanagement.presentation.student

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

class StudentViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BaseViewModel<StudentAction, StudentUiState>() {
    override fun setInitialState(): StudentUiState = StudentUiState()

    override fun onAction(event: StudentAction) {

    }

}


sealed class StudentAction {

}

data class StudentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)