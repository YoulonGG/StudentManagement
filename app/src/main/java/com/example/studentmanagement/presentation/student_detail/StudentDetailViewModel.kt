package com.example.studentmanagement.presentation.student_detail

import android.net.Uri
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.response.StudentResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
            is StudentDetailAction.LoadStudent -> setState { copy(student = event.student) }
            is StudentDetailAction.SaveStudent -> saveStudentToFirestore(event.updatedStudent)
            is StudentDetailAction.UploadImage -> uploadImageToStorage(event.imageUri)
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

        val current = uiState.value.student ?: run {
            setState { copy(error = "No student loaded") }
            return
        }

        val updatedStudent = current.copy(
            name = student.name,
            email = student.email,
            address = student.address,
            phone = student.phone,
            age = student.age,
            imageUrl = student.imageUrl,
            studentID = student.studentID,
            guardian = student.guardian,
            guardianContact = student.guardianContact,
            majoring = student.majoring,
            authUid = current.authUid
        )

        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            setState { copy(isLoading = false, error = "User not signed in") }
            return
        }

        firestore.collection("users").document(authUid).get()
            .addOnSuccessListener { document ->
                val isTeacher = document.getString("accountType") == "teacher"

                val allowedFields = if (isTeacher) {
                    mapOf(
                        "name" to updatedStudent.name,
                        "email" to updatedStudent.email,
                        "address" to updatedStudent.address,
                        "phone" to updatedStudent.phone,
                        "age" to updatedStudent.age,
                        "imageUrl" to updatedStudent.imageUrl,
                        "guardian" to updatedStudent.guardian,
                        "guardianContact" to updatedStudent.guardianContact,
                        "majoring" to updatedStudent.majoring
                    )
                } else {
                    mapOf(
                        "name" to updatedStudent.name,
                        "imageUrl" to updatedStudent.imageUrl
                    )
                }.filterValues { it != null }

                firestore.collection("students")
                    .document(updatedStudent.authUid ?: "")
                    .update(allowedFields)
                    .addOnSuccessListener {
                        setState { copy(isLoading = false, student = updatedStudent) }
                    }
                    .addOnFailureListener { e ->
                        setState { copy(isLoading = false, error = e.message) }
                    }

            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch user role: ${e.message}"
                    )
                }
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
