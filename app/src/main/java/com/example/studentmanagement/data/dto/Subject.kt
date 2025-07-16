package com.example.studentmanagement.data.dto

import android.net.Uri

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */

data class Subject(
    val id: String = "",
    val name: String,
    val description: String? = null,
    val code: String? = null,
    val className: String? = null,
    val classTime: String? = null,
    val imageUrl: String? = null,
    val localImageUri: Uri? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)