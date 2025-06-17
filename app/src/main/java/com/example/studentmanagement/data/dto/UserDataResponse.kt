package com.example.studentmanagement.data.dto

/**
 * @Author: John Youlong.
 * @Date: 5/17/25.
 * @Email: johnyoulong@gmail.com.
 */


data class UserDataResponse(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val studentId: String? = null,
    val department: String? = null,
    val accountType: String = "student",
    val courses: List<String> = emptyList()
)