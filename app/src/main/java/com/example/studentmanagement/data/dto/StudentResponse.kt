package com.example.studentmanagement.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @Author: John Youlong.
 * @Date: 6/3/25.
 * @Email: johnyoulong@gmail.com.
 */

@Parcelize
data class StudentResponse(
    val imageUrl: String? = null,
    val name: String? = null,
    val email: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val age: Int? = null,
    val isApproved: Boolean? = null,
    val studentID: String? = null,
    val guardian: String? = null,
    val guardianContact: String? = null,
    val majoring: String? = null,
    val authUid: String? = null,
    val gender: String? = null
) : Parcelable