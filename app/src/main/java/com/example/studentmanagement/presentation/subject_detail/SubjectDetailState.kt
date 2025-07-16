package com.example.studentmanagement.presentation.subject_detail

import com.example.studentmanagement.data.dto.Subject

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

data class SubjectDetailState(
    val subject: Subject? = null,
    val detailItems: List<SubjectDetailItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class SubjectDetailEvent {
    data class LoadSubjectDetail(val subjectId: String) : SubjectDetailEvent()
    data class UpdateSubject(val subject: Subject) : SubjectDetailEvent()
    object ClearError : SubjectDetailEvent()
    object ClearSuccessMessage : SubjectDetailEvent()
}

sealed class SubjectDetailItem {
    data class SectionHeader(val title: String) : SubjectDetailItem()
    data class DetailRow(val label: String, val value: String) : SubjectDetailItem()
}