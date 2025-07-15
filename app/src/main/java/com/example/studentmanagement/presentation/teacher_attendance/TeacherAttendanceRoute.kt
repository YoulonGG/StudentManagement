package com.example.studentmanagement.presentation.teacher_attendance

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */


data class TeacherAttendanceState(
    val students: List<StudentAttendance> = emptyList(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val attendanceSubmitted: Boolean = false,

    )

data class StudentAttendance(
    val studentId: String,
    val fullName: String,
    val status: AttendanceStatus,
    val hasPermissionRequest: Boolean = false,
    val permissionReason: String? = null,
    val date: String = "",
    val statusModified: Boolean = false,
    val isSubmitted: Boolean = false,
    val isStatusSelected: Boolean = false
)

sealed class TeacherAttendanceEvent {
    data object LoadStudents : TeacherAttendanceEvent()
    data class UpdateStatus(val studentId: String, val status: AttendanceStatus) :
        TeacherAttendanceEvent()

    data object SubmitAttendance : TeacherAttendanceEvent()
    data class SetDate(val date: String) : TeacherAttendanceEvent()
    data object ClearError : TeacherAttendanceEvent()
    data object ClearSuccess : TeacherAttendanceEvent()
    data class ApprovePermission(val studentId: String) : TeacherAttendanceEvent()
    data class RejectPermission(val studentId: String) : TeacherAttendanceEvent()
}

enum class AttendanceStatus {
    PRESENT, ABSENT, PERMISSION
}