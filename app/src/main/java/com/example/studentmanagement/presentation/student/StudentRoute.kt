package com.example.studentmanagement.presentation.student

import com.example.studentmanagement.data.dto.StudentResponse

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

sealed class StudentAction {
    data object LoadStudentData : StudentAction()
}

data class StudentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null,
    val studentName: String? = null,
    val studentImage: String = "",
    val totalStudents: Int = 0,
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0,
)