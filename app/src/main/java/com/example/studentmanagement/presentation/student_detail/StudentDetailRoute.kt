package com.example.studentmanagement.presentation.student_detail

import android.net.Uri
import com.example.studentmanagement.data.dto.StudentResponse

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


sealed class StudentDetailAction {
    data class LoadStudent(val student: StudentResponse) : StudentDetailAction()
    data class SaveStudent(val updatedStudent: StudentResponse) : StudentDetailAction()
    data class UploadImage(val imageUri: Uri) : StudentDetailAction()
    data object LoadCurrentStudent : StudentDetailAction()
    data object ClearError : StudentDetailAction()
}

data class StudentDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null
)