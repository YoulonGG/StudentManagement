package com.example.studentmanagement.presentation.attendace_history

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

data class AttendanceHistoryState(
    val monthlyStats: List<MonthlyAttendanceStats> = emptyList(),
    val selectedMonth: String = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AttendanceHistoryEvent {
    data class LoadMonthStats(val monthYear: String) : AttendanceHistoryEvent()
    data object ClearError : AttendanceHistoryEvent()
}

data class MonthlyAttendanceStats(
    val studentId: String,
    val studentName: String,
    val totalDays: Int,
    val presentCount: Int,
    val absentCount: Int,
    val permissionCount: Int
)