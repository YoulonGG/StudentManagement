package com.example.studentmanagement.presentation.student_list

/**
 * @Author: John Youlong.
 * @Date: 7/17/25.
 * @Email: johnyoulong@gmail.com.
 */

sealed class StudentListAction {
    data object StudentList : StudentListAction()
    data class SearchStudents(val query: String) : StudentListAction()
}

data class StudentListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)
