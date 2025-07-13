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


// StudentPermissionViewModel.kt
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

                db.collection("permission_requests")
                    .whereEqualTo("studentId", currentUser.uid)
                    .whereEqualTo("date", uiState.value.selectedDate)
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

                        val request = hashMapOf(
                            "studentId" to currentUser.uid,
                            "studentName" to studentName,
                            "date" to uiState.value.selectedDate,
                            "reason" to uiState.value.reason,
                            "status" to PermissionStatus.PENDING.name,
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
    }
}

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