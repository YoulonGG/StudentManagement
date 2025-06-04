package com.example.studentmanagement.presentation.student_detail

import android.net.Uri
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.response.StudentResponse
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

/**
 * @Author: John Youlong.
 * @Date: 6/2/25.
 * @Email: johnyoulong@gmail.com.
 */

class StudentDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : BaseViewModel<StudentDetailAction, StudentDetailUiState>() {

    override fun setInitialState(): StudentDetailUiState = StudentDetailUiState()

    override fun onAction(event: StudentDetailAction) {
        when (event) {
            is StudentDetailAction.LoadStudent -> {
                setState { copy(student = event.student) }
            }

            is StudentDetailAction.SaveStudent -> {
                saveStudentToFirestore(event.updatedStudent)
            }

            is StudentDetailAction.UploadImage -> {
                uploadImageToStorage(event.imageUri)
            }
        }
    }

    private fun uploadImageToStorage(uri: Uri) {
        val studentId = uiState.value.student?.studentID
        if (studentId == null) {
            setState { copy(error = "Student ID is missing") }
            return
        }

        setState { copy(isLoading = true) }

        val storageRef = storage.reference.child("students/$studentId/profile.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val updatedStudent =
                        uiState.value.student?.copy(imageUrl = downloadUrl.toString())
                    if (updatedStudent != null) {
                        saveStudentToFirestore(updatedStudent)
                    } else {
                        setState {
                            copy(
                                isLoading = false,
                                error = "Failed to update student data"
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                setState { copy(isLoading = false, error = e.message) }
            }
    }

    private fun saveStudentToFirestore(student: StudentResponse) {
        setState { copy(isLoading = true, error = null) }

        student.studentID?.let { id ->
            firestore.collection("students")
                .document(id)
                .set(
                    student,
                    SetOptions.merge()
                )
                .addOnSuccessListener {
                    setState { copy(isLoading = false, student = student) }
                }
                .addOnFailureListener { e ->
                    setState { copy(isLoading = false, error = e.message) }
                }

        } ?: run {
            setState { copy(error = "Student ID is missing") }
        }
    }
}


sealed class StudentDetailAction {
    data class LoadStudent(val student: StudentResponse) : StudentDetailAction()
    data class SaveStudent(val updatedStudent: StudentResponse) : StudentDetailAction()
    data class UploadImage(val imageUri: Uri) : StudentDetailAction()
}


data class StudentDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null
)
