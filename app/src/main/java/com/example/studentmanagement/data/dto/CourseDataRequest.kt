package com.example.studentmanagement.data.dto

/**
 * @Author: John Youlong.
 * @Date: 5/17/25.
 * @Email: johnyoulong@gmail.com.
 */

data class CourseDataRequest(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val instructorId: String = "",
    val instructorName: String = ""
)
