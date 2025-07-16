package com.example.studentmanagement.presentation.ask_permission

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 6/16/25.
 * @Email: johnyoulong@gmail.com.
 */

@Suppress("LABEL_NAME_CLASH")
class StudentPermissionViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BaseViewModel<StudentPermissionEvent, StudentPermissionState>() {

    override fun setInitialState() = StudentPermissionState()

    override fun onAction(event: StudentPermissionEvent) {
        when (event) {
            is StudentPermissionEvent.SetDate -> setState { copy(selectedDate = event.date) }
            is StudentPermissionEvent.SetReason -> setState { copy(reason = event.reason) }
            is StudentPermissionEvent.SubmitRequest -> submitPermissionRequest()
            StudentPermissionEvent.ClearError -> setState { copy(error = null) }
        }
    }

    private fun submitPermissionRequest() {
        val currentUser = auth.currentUser ?: run {
            setState { copy(error = "You must be logged in to submit a request") }
            return
        }

        if (uiState.value.selectedDate.isBlank()) {
            setState { copy(error = "Please select a date") }
            return
        }

        if (uiState.value.reason.isBlank()) {
            setState { copy(error = "Please enter a reason") }
            return
        }

        setState { copy(isLoading = true, error = null) }

        db.collection("students")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { studentDoc ->
                val studentName = studentDoc.getString("name") ?: run {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Student information not found"
                        )
                    }
                    return@addOnSuccessListener
                }

                checkTeacherSubmission(currentUser.uid, studentName)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to get student information: ${e.message}"
                    )
                }
            }
    }

    private fun checkTeacherSubmission(studentId: String, studentName: String) {
        val selectedDate = uiState.value.selectedDate

        db.collection("attendance")
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { attendanceSnapshot ->
                if (!attendanceSnapshot.isEmpty) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Cannot request permission for this date. Teacher has already submitted attendance."
                        )
                    }
                    return@addOnSuccessListener
                }

                checkExistingPermissionRequest(studentId, studentName)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to check teacher submission: ${e.message}"
                    )
                }
            }
    }

    private fun checkExistingPermissionRequest(studentId: String, studentName: String) {
        val selectedDate = uiState.value.selectedDate

        // Check if student already has a permission request for this date
        db.collection("permission_requests")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("date", selectedDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "You already have a permission request for this date"
                        )
                    }
                    return@addOnSuccessListener
                }

                createPermissionRequest(studentId, studentName)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to check existing requests: ${e.message}"
                    )
                }
            }
    }

    private fun createPermissionRequest(studentId: String, studentName: String) {
        val request = hashMapOf(
            "studentId" to studentId,
            "studentName" to studentName,
            "date" to uiState.value.selectedDate,
            "reason" to uiState.value.reason,
            "status" to PermissionStatus.PENDING.name,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("permission_requests")
            .add(request)
            .addOnSuccessListener {
                setState {
                    copy(
                        isLoading = false,
                        success = true,
                        selectedDate = "",
                        reason = ""
                    )
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to submit request: ${e.message}"
                    )
                }
            }
    }
}