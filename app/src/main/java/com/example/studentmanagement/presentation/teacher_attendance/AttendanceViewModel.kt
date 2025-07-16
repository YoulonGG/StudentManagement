package com.example.studentmanagement.presentation.teacher_attendance

import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.presentation.ask_permission.PermissionStatus
import com.example.studentmanagement.presentation.teacher_attendance.components.AttendanceAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 6/10/25.
 * @Email: johnyoulong@gmail.com.
 */

class TeacherAttendanceViewModel(
    private val db: FirebaseFirestore, private val auth: FirebaseAuth
) : BaseViewModel<TeacherAttendanceEvent, TeacherAttendanceState>() {

    val attendanceAdapter = AttendanceAdapter(onStatusChanged = { studentId, status ->
        onAction(TeacherAttendanceEvent.UpdateStatus(studentId, status))
    }, onPermissionAction = { studentId, approved ->
        onAction(
            if (approved) TeacherAttendanceEvent.ApprovePermission(studentId)
            else TeacherAttendanceEvent.RejectPermission(studentId)
        )
    })

    override fun setInitialState(): TeacherAttendanceState = TeacherAttendanceState()


    override fun onAction(event: TeacherAttendanceEvent) {
        when (event) {
            is TeacherAttendanceEvent.LoadStudents -> loadStudents()
            is TeacherAttendanceEvent.UpdateStatus -> updateStatus(event.studentId, event.status)
            is TeacherAttendanceEvent.SubmitAttendance -> submitAttendance()
            is TeacherAttendanceEvent.SetDate -> {
                setState { copy(selectedDate = event.date) }
                checkAttendanceExists(event.date)
            }

            TeacherAttendanceEvent.ClearError -> setState { copy(error = null) }
            TeacherAttendanceEvent.ClearSuccess -> setState { copy(submissionSuccess = false) }
            is TeacherAttendanceEvent.ApprovePermission -> handlePermission(event.studentId, true)
            is TeacherAttendanceEvent.RejectPermission -> handlePermission(event.studentId, false)
        }
    }

    private fun loadStudents() {
        setState { copy(isLoading = true) }

        db.collection("permission_requests")
            .whereEqualTo("date", uiState.value.selectedDate)
            .get()
            .addOnSuccessListener { permissionSnapshot ->
                val permissionRequests = permissionSnapshot.documents.associate { doc ->
                    doc.getString("studentId")!! to PermissionRequestInfo(
                        reason = doc.getString("reason") ?: "",
                        status = doc.getString("status")?.let {
                            PermissionStatus.valueOf(it)
                        } ?: PermissionStatus.PENDING
                    )
                }

                db.collection("students").get()
                    .addOnSuccessListener { snapshot ->
                        val students = snapshot.documents.map { doc ->
                            val permissionInfo = permissionRequests[doc.id]
                            val savedStatus = savedStatuses[doc.id]
                            StudentAttendance(
                                studentId = doc.id,
                                fullName = doc.getString("name") ?: "Unknown",
                                status = when {
                                    savedStatus != null -> savedStatus
                                    permissionInfo?.status == PermissionStatus.APPROVED -> AttendanceStatus.PERMISSION
                                    permissionInfo?.status == PermissionStatus.REJECTED -> AttendanceStatus.ABSENT
                                    else -> AttendanceStatus.PRESENT
                                },
                                hasPermissionRequest = permissionInfo?.status == PermissionStatus.PENDING,
                                permissionReason = permissionInfo?.reason,
                                date = uiState.value.selectedDate,
                                statusModified = savedStatus != null ||
                                        permissionInfo?.status == PermissionStatus.APPROVED ||
                                        permissionInfo?.status == PermissionStatus.REJECTED,
                                isStatusSelected = savedStatus != null ||
                                        permissionInfo?.status == PermissionStatus.APPROVED ||
                                        permissionInfo?.status == PermissionStatus.REJECTED
                            )
                        }
                        setState {
                            copy(
                                students = students,
                                isLoading = false,
                                error = null
                            )
                        }
                        attendanceAdapter.submitList(students)
                    }
            }
    }

    private data class PermissionRequestInfo(
        val reason: String,
        val status: PermissionStatus
    )

    private fun handlePermission(studentId: String, approved: Boolean) {
        setState { copy(isLoading = true) }

        db.collection("permission_requests")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("date", uiState.value.selectedDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val document = snapshot.documents.first()
                    document.reference.update(
                        mapOf(
                            "status" to if (approved) PermissionStatus.APPROVED.name
                            else PermissionStatus.REJECTED.name,
                            "responseTimestamp" to FieldValue.serverTimestamp()
                        )
                    ).addOnSuccessListener {
                        val updatedStudents = uiState.value.students.map {
                            if (it.studentId == studentId) {
                                it.copy(
                                    hasPermissionRequest = false,
                                    status = if (approved) AttendanceStatus.PERMISSION
                                    else AttendanceStatus.ABSENT,
                                    statusModified = true,
                                    isStatusSelected = true
                                )
                            } else it
                        }
                        setState {
                            copy(
                                students = updatedStudents,
                                isLoading = false
                            )
                        }
                        attendanceAdapter.submitList(updatedStudents)

                        savedStatuses[studentId] = if (approved)
                            AttendanceStatus.PERMISSION else AttendanceStatus.ABSENT
                    }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to update permission: ${e.message}"
                    )
                }
            }
    }

    private fun updateStatus(studentId: String, status: AttendanceStatus) {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }

        val student = uiState.value.students.find { it.studentId == studentId }
        if (student?.status == AttendanceStatus.PERMISSION) {
            setState { copy(error = "Cannot change status of approved permission") }
            return
        }

        savedStatuses[studentId] = status

        val updatedStudents = uiState.value.students.map {
            if (it.studentId == studentId) {
                it.copy(
                    status = status,
                    statusModified = true,
                    isStatusSelected = true
                )
            } else it
        }
        setState { copy(students = updatedStudents) }
        attendanceAdapter.submitList(updatedStudents)
    }

    private fun submitAttendance() {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }

        if (!uiState.value.students.all { it.isStatusSelected }) {
            setState { copy(error = "Please select status for all students") }
            return
        }

        val currentUser = auth.currentUser ?: run {
            setState { copy(error = "You must be logged in to submit attendance") }
            return
        }

        setState { copy(isSubmitting = true, error = null) }

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                proceedWithSubmission(currentUser.uid)
            } else {
                setState {
                    copy(
                        isSubmitting = false, error = "Only teachers can submit attendance"
                    )
                }
            }
        }.addOnFailureListener { e ->
            setState {
                copy(
                    isSubmitting = false, error = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    private fun proceedWithSubmission(teacherId: String) {
        val batch = db.batch()
        val attendanceRef = db.collection("attendance").document(uiState.value.selectedDate)

        batch.set(
            attendanceRef, hashMapOf(
                "date" to uiState.value.selectedDate,
                "teacherId" to teacherId,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )

        uiState.value.students.forEach { student ->
            val studentRef =
                attendanceRef.collection("attendance_records").document(student.studentId)
            batch.set(
                studentRef, hashMapOf(
                    "status" to student.status.name,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "teacherId" to teacherId
                )
            )
        }

        batch.commit()
            .addOnSuccessListener {
                val updatedStudents = uiState.value.students.map {
                    it.copy(
                        isSubmitted = true,
                        status = it.status
                    )
                }
                setState {
                    copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        attendanceSubmitted = true,
                        students = updatedStudents
                    )
                }
                attendanceAdapter.submitList(updatedStudents)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isSubmitting = false,
                        error = "Submission failed: ${e.message}"
                    )
                }
            }
    }

    private fun checkAttendanceExists(date: String) {
        setState { copy(isLoading = true) }

        db.collection("attendance").document(date).collection("attendance_records").limit(1).get()
            .addOnSuccessListener { snapshot ->
                setState {
                    copy(
                        isLoading = false, attendanceSubmitted = !snapshot.isEmpty
                    )
                }
                if (!snapshot.isEmpty) {
                    loadSubmittedAttendance(date)
                } else {
                    loadStudents()
                }
            }.addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false, error = "Error checking attendance: ${e.message}"
                    )
                }
            }
    }

    private fun loadSubmittedAttendance(date: String) {
        setState { copy(isLoading = true) }

        db.collection("attendance").document(date).collection("attendance_records").get()
            .addOnSuccessListener { snapshot ->
                val submittedRecords = snapshot.documents.associate { doc ->
                    doc.id to (doc.getString("status")?.let {
                        AttendanceStatus.valueOf(it)
                    } ?: AttendanceStatus.PRESENT)
                }

                db.collection("students").get().addOnSuccessListener { studentsSnapshot ->
                    val students = studentsSnapshot.documents.map { doc ->
                        StudentAttendance(
                            studentId = doc.id,
                            fullName = doc.getString("name") ?: "Unknown",
                            status = submittedRecords[doc.id] ?: AttendanceStatus.PRESENT,
                            isSubmitted = true
                        )
                    }
                    setState {
                        copy(
                            students = students,
                            isLoading = false,
                            error = null,
                            attendanceSubmitted = true
                        )
                    }
                    attendanceAdapter.submitList(students)
                }
            }.addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load submitted attendance: ${e.message}"
                    )
                }
            }
    }

    companion object {
        private val savedStatuses = mutableMapOf<String, AttendanceStatus>()
    }
}
