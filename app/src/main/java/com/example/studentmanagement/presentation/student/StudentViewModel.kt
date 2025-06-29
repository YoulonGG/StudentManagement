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
        setState { copy(isLoading = true, error = null) }

        firestore.collection("students")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val student = document.toObject(StudentResponse::class.java)
                    setState {
                        copy(
                            isLoading = false,
                            student = student,
                            studentName = student?.name ?: "N/A",
                            studentImage = student?.imageUrl ?: "",
                            error = null
                        )
                    }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Student data not found"
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load student data"
                    )
                }
            }
    }

    fun clearError() {
        setState { copy(error = null) }
    }

    fun refreshStudentData() {
        getStudentDetails()
    }
}

sealed class StudentAction {
    data object LoadStudentData : StudentAction()
}

data class StudentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null,
    val studentName: String? = null,
    val studentImage: String = ""
)


