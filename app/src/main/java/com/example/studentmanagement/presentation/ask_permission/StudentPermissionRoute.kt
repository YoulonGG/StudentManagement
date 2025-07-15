package com.example.studentmanagement.presentation.ask_permission

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class StudentPermissionState(
    val selectedDate: String = "",
    val reason: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

sealed class StudentPermissionEvent {
    data class SetDate(val date: String) : StudentPermissionEvent()
    data class SetReason(val reason: String) : StudentPermissionEvent()
    data object SubmitRequest : StudentPermissionEvent()
    data object ClearError : StudentPermissionEvent()
}

enum class PermissionStatus {
    PENDING,
    APPROVED,
    REJECTED
}