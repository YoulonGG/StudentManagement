package com.example.studentmanagement.data.dto

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */

data class Subject(
    val id: String = "",
    val name: String = "",
    val description: String? = "",
    val imageUrl: String? = "",
    val teacherId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)