package com.example.studentmanagement.presentation.create_student

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

data class CreateStudentUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val duplicateStudentId: String? = null
)

sealed interface CreateStudentAction {
    data class SubmitStudent(
        val email: String,
        val name: String,
        val studentID: String,
        val gender: String
    ) : CreateStudentAction
}