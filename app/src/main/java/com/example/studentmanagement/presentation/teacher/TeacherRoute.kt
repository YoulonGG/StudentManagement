package com.example.studentmanagement.presentation.teacher

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class TeacherUiState(
    val isLoading: Boolean = false,
    val teacherName: String = "",
    val totalStudents: Int = 0,
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0,
    val profileImageUrl: String? = null,
    val error: String? = null
)

sealed interface TeacherAction {
    data object LoadTeacherData : TeacherAction
    data object LoadStudentCounts : TeacherAction
}

data class HomeCardItem(
    val id: Int,
    val title: String,
    val icon: Int,
    val onClick: () -> Unit
)