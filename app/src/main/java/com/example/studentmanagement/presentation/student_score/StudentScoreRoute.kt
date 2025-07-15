package com.example.studentmanagement.presentation.student_score

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

data class StudentScoreViewUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface StudentScoreViewAction {
    data class LoadStudentScores(val studentId: String) : StudentScoreViewAction
    data class RefreshScores(val studentId: String) : StudentScoreViewAction
}

data class StudentInfo(
    val id: String,
    val name: String,
    val email: String,
    val studentId: String
)

data class StudentScoreDetail(
    val subject: String,
    val assignment: Float,
    val homework: Float,
    val midterm: Float,
    val final: Float,
    val total: Float,
    val timestamp: com.google.firebase.Timestamp?
) {
    val percentage: Float
        get() = (total / 400) * 100

    val grade: String
        get() = when {
            percentage >= 90 -> "A"
            percentage >= 80 -> "B"
            percentage >= 70 -> "C"
            percentage >= 60 -> "D"
            else -> "F"
        }
}