package com.example.studentmanagement.data.dto

/**
 * @Author: John Youlong.
 * @Date: 5/17/25.
 * @Email: johnyoulong@gmail.com.
 */


data class EnrollmentDataRequest(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val enrollmentDate: String = "",
    val grade: String? = null
)