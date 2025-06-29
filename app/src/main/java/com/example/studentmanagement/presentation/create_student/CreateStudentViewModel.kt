package com.example.studentmanagement.presentation.create_student

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

            // Check if student ID already exists
            if (!checkStudentIdAvailability(studentID)) return@launch

            setState { copy(isLoading = true, error = null) }
            try {
                val result = signUpStudent(email, password, name, studentID, gender)
                setState {
                    copy(
                        isLoading = false,
                        success = result.isSuccess,
                        error = result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to create student: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private suspend fun checkStudentIdAvailability(studentID: String): Boolean = try {
        setState { copy(isLoading = true, error = null) }

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
            false
        } else {
            true
        }
    } catch (e: Exception) {
        setState {
            copy(
                isLoading = false,
                error = "Failed to verify Student ID availability: ${e.message ?: "Unknown error"}"
            )
        }
        false
    }

    private suspend fun signUpStudent(
        email: String,
        password: String,
        name: String,
        studentID: String,
        gender: String
    ): Result<Unit> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("User creation failed")

        val studentData = hashMapOf(
            "email" to email,
            "name" to name,
            "password" to password,
            "studentID" to studentID,
            "authUid" to user.uid,
            "accountType" to "student",
            "createdAt" to FieldValue.serverTimestamp(),
            "imageUrl" to "",
            "address" to "",
            "phone" to "",
            "age" to null,
            "guardian" to "",
            "guardianContact" to "",
            "majoring" to "Computer Science and Engineering",
            "gender" to gender,
            "status" to "active",
            "lastLogin" to FieldValue.serverTimestamp()
        )

        firestore.collection("students")
            .document(user.uid)
            .set(studentData)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        auth.currentUser?.delete()?.await()
        Result.failure(Exception("Student registration failed: ${e.message}"))
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

data class CreateStudentUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val duplicateStudentId: String? = null
)

sealed interface CreateStudentAction {
    data class SubmitStudent(
        val email: String,
        val name: String,
        val studentID: String,
        val gender: String
    ) : CreateStudentAction
}
