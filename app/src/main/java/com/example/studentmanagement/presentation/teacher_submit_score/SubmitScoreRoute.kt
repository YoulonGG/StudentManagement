package com.example.studentmanagement.presentation.teacher_submit_score

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class SubmitScoreUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val submitSuccess: Boolean = false
)

sealed interface SubmitScoreAction

data class StudentScore(
    val studentId: String,
    val name: String,
    var assignment: Float = 0f,
    var midterm: Float = 0f,
    var final: Float = 0f,
    var homework: Float = 0f
) {
    val total: Float
        get() = assignment + midterm + final + homework
}