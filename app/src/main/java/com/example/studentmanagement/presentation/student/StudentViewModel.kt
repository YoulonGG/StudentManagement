package com.example.studentmanagement.presentation.student

import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.StudentResponse
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
        when (event) {
            StudentAction.LoadStudentData -> getStudentDetails()
        }
    }

    private fun getStudentDetails() {
        val currentUser = auth.currentUser ?: return
        setState { copy(isLoading = true) }

        firestore.collection("students")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val student = document.toObject(StudentResponse::class.java)
                setState {
                    copy(
                        isLoading = false,
                        student = student
                    )
                }
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            }
    }
}


sealed class StudentAction {
    data object LoadStudentData : StudentAction()
}


data class StudentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null
)


