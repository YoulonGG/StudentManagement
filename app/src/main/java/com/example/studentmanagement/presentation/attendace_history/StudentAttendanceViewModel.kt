package com.example.studentmanagement.presentation.attendace_history

import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * @Author: John Youlong.
 * @Date: 6/13/25.
 * @Email: johnyoulong@gmail.com.
 */

class StudentAttendanceViewModel(
    private val db: FirebaseFirestore
) : BaseViewModel<AttendanceHistoryEvent, AttendanceHistoryState>() {

    override fun setInitialState(): AttendanceHistoryState = AttendanceHistoryState()

    override fun onAction(event: AttendanceHistoryEvent) {
        when (event) {
            is AttendanceHistoryEvent.LoadMonthStats -> {
                loadMonthlyStats(event.monthYear)
            }

            AttendanceHistoryEvent.ClearError -> {
                setState { copy(error = null) }
            }
        }
    }

    private fun loadMonthlyStats(monthYear: String) {
        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {

                val calendar = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                calendar.time = sdf.parse(monthYear) ?: Date()

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)


                val studentsSnapshot = db.collection("students")
                    .get()
                    .await()

                val studentsMap = studentsSnapshot.documents.associate {
                    it.id to (it.getString("name") ?: "Unknown")
                }

                val attendanceSnapshot = db.collection("attendance")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), startDate)
                    .whereLessThanOrEqualTo(FieldPath.documentId(), endDate)
                    .get()
                    .await()

                val studentStats = mutableMapOf<String, MutableMap<String, Int>>()

                studentsMap.keys.forEach { studentId ->
                    studentStats[studentId] = mutableMapOf(
                        "present" to 0,
                        "absent" to 0,
                        "permission" to 0
                    )
                }

                attendanceSnapshot.documents.forEach { doc ->
                    val records = doc.reference.collection("attendance_records")
                        .get()
                        .await()

                    records.documents.forEach { record ->
                        val studentId = record.id
                        val status = record.getString("status")?.uppercase() ?: "PRESENT"

                        studentStats[studentId]?.let { stats ->
                            when (status) {
                                "PRESENT" -> stats["present"] = (stats["present"] ?: 0) + 1
                                "ABSENT" -> stats["absent"] = (stats["absent"] ?: 0) + 1
                                "PERMISSION" -> stats["permission"] = (stats["permission"] ?: 0) + 1
                            }
                        }
                    }
                }

                val stats = studentsMap.map { (studentId, name) ->
                    val counts = studentStats[studentId] ?: mutableMapOf()
                    MonthlyAttendanceStats(
                        studentId = studentId,
                        studentName = name,
                        totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                        presentCount = counts["present"] ?: 0,
                        absentCount = counts["absent"] ?: 0,
                        permissionCount = counts["permission"] ?: 0
                    )
                }
                setState {
                    copy(
                        isLoading = false,
                        monthlyStats = stats,
                        selectedMonth = monthYear,
                        error = null
                    )
                }
            } catch (e: Exception) {
                println("Error loading stats: ${e.message}")
                e.printStackTrace()
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }}

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
