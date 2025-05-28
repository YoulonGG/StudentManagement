package com.example.studentmanagement.presentation.create_student

import com.example.studentmanagement.data.dto.request.CreateStudentDataRequest

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */


sealed class CreateStudentAction {
    class OnCreateStudent(val data: CreateStudentDataRequest) : CreateStudentAction()
}

data class CreateStudentState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)