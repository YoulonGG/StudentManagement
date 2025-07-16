package com.example.studentmanagement.presentation.subjects_list

import android.net.Uri
import com.example.studentmanagement.data.dto.Subject

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class SubjectListState(
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class SubjectListEvent {
    data object LoadSubjects : SubjectListEvent()
    data object ClearError : SubjectListEvent()
    data object ClearSuccessMessage : SubjectListEvent()
    data class CreateSubject(
        val name: String,
        val description: String,
        val code: String,
        val className: String,
        val classTime: String,
        val imageUri: Uri?
    ) : SubjectListEvent()
    data class DeleteSubject(val subjectId: String) : SubjectListEvent()
}