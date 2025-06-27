package com.example.studentmanagement.presentation.create_student

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateStudentViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateStudentUiState())
    val uiState: StateFlow<CreateStudentUiState> = _uiState

    fun createStudent(
        email: String,
        password: String,
        name: String,
        studentID: String,
        gender: String
    ) {
        viewModelScope.launch {
            if (!validateInputs(email, password, name, studentID, gender)) return@launch
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = signUpStudent(email, password, name, studentID, gender)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    success = result.isSuccess,
                    error = result.exceptionOrNull()?.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Student creation failed: ${e.message}"
                )
            }
        }
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
            "isApproved" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "imageUrl" to "",
            "address" to "",
            "phone" to "",
            "age" to null,
            "guardian" to "",
            "guardianContact" to "",
            "majoring" to "Computer Science and Engineering",
            "gender" to gender,
            "status" to "pending",
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
        password: String,
        name: String,
        studentID: String,
        gender: String
    ): Boolean = when {
        email.trim().isEmpty() -> {
            _uiState.value = _uiState.value.copy(error = "Email cannot be empty")
            false
        }
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
            _uiState.value = _uiState.value.copy(error = "Invalid email format")
            false
        }
        password.trim().isEmpty() -> {
            _uiState.value = _uiState.value.copy(error = "Password cannot be empty")
            false
        }
        password.length < 6 -> {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            false
        }
        name.trim().isEmpty() -> {
            _uiState.value = _uiState.value.copy(error = "Name cannot be empty")
            false
        }
        studentID.trim().isEmpty() -> {
            _uiState.value = _uiState.value.copy(error = "Student ID cannot be empty")
            false
        }
        !studentID.matches(Regex("^[A-Za-z0-9]+$")) -> {
            _uiState.value = _uiState.value.copy(error = "Invalid Student ID format")
            false
        }
        gender.isEmpty() -> {
            _uiState.value = _uiState.value.copy(error = "Please select gender")
            false
        }
        else -> true
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = CreateStudentUiState()
    }
}

data class CreateStudentUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

