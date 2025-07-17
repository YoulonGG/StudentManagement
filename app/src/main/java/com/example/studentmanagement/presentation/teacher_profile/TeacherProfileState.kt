package com.example.studentmanagement.presentation.teacher_profile

import android.net.Uri
import com.example.studentmanagement.data.dto.TeacherResponse

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class TeacherProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val teacher: TeacherResponse? = null
)

sealed class TeacherProfileAction {
    data class LoadTeacher(val teacher: TeacherResponse) : TeacherProfileAction()
    data object LoadCurrentTeacher : TeacherProfileAction()
    data class SaveTeacher(val updatedTeacher: TeacherResponse) : TeacherProfileAction()
    data class UploadImage(val imageUri: Uri) : TeacherProfileAction()
}