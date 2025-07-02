package com.example.studentmanagement.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TeacherResponse(
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val imageUrl: String? = null,
    val address: String? = null,
    val age: String? = null,
    val teacherClass: String? = null,
    val gender: String? = null
) : Parcelable
