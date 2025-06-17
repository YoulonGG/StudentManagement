package com.example.studentmanagement.data.dto

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */

data class Homework(
    val id: String = "",
    val title: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val submissionUrl: String? = null,
    val submissionFileName: String? = null,
    val uploadedAt: Long = System.currentTimeMillis()
)