package com.example.studentmanagement.data.dto.request

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

data class CreateStudentDataRequest(
    val fullName: String? = null,
    val email: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val age: Int? = null,
    val studentId: String? = null,
    val guardian: String? = null,
    val guardianContact: String? = null,
    val majoring: String? = null
)