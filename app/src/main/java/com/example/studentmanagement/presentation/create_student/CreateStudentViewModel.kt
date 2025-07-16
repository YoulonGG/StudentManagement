package com.example.studentmanagement.presentation.create_student

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateStudentViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BaseViewModel<CreateStudentAction, CreateStudentUiState>() {

    override fun setInitialState(): CreateStudentUiState = CreateStudentUiState()

    override fun onAction(event: CreateStudentAction) {
        when (event) {
            is CreateStudentAction.SubmitStudent -> {
                createStudent(
                    email = event.email,
                    name = event.name,
                    studentID = event.studentID,
                    gender = event.gender
                )
            }
        }
    }

    private fun createStudent(
        email: String,
        name: String,
        studentID: String,
        gender: String,
    ) {
        val password = "aib123"

        viewModelScope.launch {
            if (!validateInputs(email, name, studentID, gender)) return@launch
            setState { copy(isLoading = true, error = null) }

            try {
                val querySnapshot = firestore.collection("students")
                    .whereEqualTo("studentID", studentID)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "DUPLICATE_STUDENT_ID",
                            duplicateStudentId = studentID
                        )
                    }
                    return@launch
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to verify Student ID availability: ${e.message ?: "Unknown error"}"
                    )
                }
                return@launch
            }

            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: throw Exception("User creation failed")

                val studentData = hashMapOf(
                    "email" to email,
                    "name" to name,
                    "password" to password,
                    "studentID" to studentID,
                    "authUid" to user.uid,
                    "accountType" to "student",
                    "imageUrl" to "",
                    "address" to "",
                    "phone" to "",
                    "age" to null,
                    "guardian" to "",
                    "guardianContact" to "",
                    "majoring" to "Computer Science and Engineering",
                    "gender" to gender,
                )

                firestore.collection("students")
                    .document(user.uid)
                    .set(studentData)
                    .await()

                setState {
                    copy(isLoading = false, success = true, error = null)
                }

            } catch (e: Exception) {
                auth.currentUser?.delete()?.await()
                setState {
                    copy(
                        isLoading = false,
                        error = "Student registration failed: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun validateInputs(
        email: String,
        name: String,
        studentID: String,
        gender: String
    ): Boolean = when {
        email.trim().isEmpty() -> {
            setState { copy(error = "Email cannot be empty") }
            false
        }

        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            setState { copy(error = "Invalid email format") }
            false
        }

        name.trim().isEmpty() -> {
            setState { copy(error = "Name cannot be empty") }
            false
        }

        studentID.trim().isEmpty() -> {
            setState { copy(error = "Student ID cannot be empty") }
            false
        }

        !studentID.matches(Regex("^[A-Za-z0-9]+$")) -> {
            setState { copy(error = "Invalid Student ID format") }
            false
        }

        gender.isEmpty() -> {
            setState { copy(error = "Please select gender") }
            false
        }

        else -> true
    }

    fun clearError() {
        setState { copy(error = null) }
    }

    fun resetState() {
        setState { CreateStudentUiState() }
    }
}
