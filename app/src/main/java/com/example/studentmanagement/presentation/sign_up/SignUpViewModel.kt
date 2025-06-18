package com.example.studentmanagement.presentation.sign_up

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

class SignUpViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BaseViewModel<SignUpAction, SignUpUiState>() {

    override fun setInitialState(): SignUpUiState = SignUpUiState()

    override fun onAction(event: SignUpAction) {
        when (event) {
            is SignUpAction.SubmitTeacher -> handleTeacherSignUp(
                email = event.email,
                password = event.password,
                gender = event.gender
            )
            is SignUpAction.SubmitStudent -> handleStudentSignUp(
                email = event.email,
                password = event.password,
                name = event.name,
                studentID = event.studentID,
                gender = event.gender
            )
            SignUpAction.ClearError -> setState { copy(error = null) }
            SignUpAction.ResetState -> setState { SignUpUiState() }
        }
    }

    private fun handleTeacherSignUp(email: String, password: String, gender: String) {
        viewModelScope.launch {
            try {
                if (!validateTeacherInputs(email, password, gender)) return@launch
                setState { copy(isLoading = true, error = null) }

                val result = signUpTeacher(email, password, gender)
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
                        error = "Registration failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun handleStudentSignUp(
        email: String,
        password: String,
        name: String,
        studentID: String,
        gender: String
    ) {
        viewModelScope.launch {
            try {
                if (!validateStudentInputs(email, password, name, studentID, gender)) return@launch
                setState { copy(isLoading = true, error = null) }

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
                        error = "Registration failed: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun signUpTeacher(
        email: String,
        password: String,
        gender: String
    ): Result<Unit> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("User creation failed")

        val teacherData = hashMapOf(
            "email" to email,
            "accountType" to "teacher",
            "createdAt" to FieldValue.serverTimestamp(),
            "gender" to gender,
            "authUid" to user.uid,
            "imageUrl" to "",
            "status" to "active",
            "lastLogin" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(user.uid)
            .set(teacherData)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        auth.currentUser?.delete()?.await()
        Result.failure(Exception("Teacher registration failed: ${e.message}"))
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
            "studentID" to studentID,
            "authUid" to user.uid,
            "accountType" to "student",
            "isApproved" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "imageUrl" to "",
            "address" to "",
            "phone" to "",
            "age" to "",
            "guardian" to "",
            "guardianContact" to "",
            "majoring" to "Computer Science and Engineering",
            "gender" to gender,
            "status" to "pending",
            "lastLogin" to FieldValue.serverTimestamp()
        )

        try {
            firestore.collection("students")
                .document(user.uid)
                .set(studentData)
                .await()
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            throw Exception("Student ID might already exist or other error: ${e.message}")
        }

        Result.success(Unit)
    } catch (e: Exception) {
        auth.currentUser?.delete()?.await()
        Result.failure(Exception("Student registration failed: ${e.message}"))
    }

    private fun validateTeacherInputs(
        email: String,
        password: String,
        gender: String
    ): Boolean = when {
        !validateBasicInputs(email, password) -> false
        gender.isEmpty() -> {
            setState { copy(error = "Please select gender") }
            false
        }
        else -> true
    }

    private fun validateStudentInputs(
        email: String,
        password: String,
        name: String,
        studentID: String,
        gender: String
    ): Boolean = when {
        !validateBasicInputs(email, password) -> false
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

    private fun validateBasicInputs(email: String, password: String): Boolean = when {
        email.trim().isEmpty() -> {
            setState { copy(error = "Email cannot be empty") }
            false
        }
        !isValidEmail(email) -> {
            setState { copy(error = "Invalid email format") }
            false
        }
        password.trim().isEmpty() -> {
            setState { copy(error = "Password cannot be empty") }
            false
        }
        password.length < 6 -> {
            setState { copy(error = "Password must be at least 6 characters") }
            false
        }
        else -> true
    }

    private fun isValidEmail(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

sealed class SignUpAction {
    data class SubmitTeacher(
        val email: String,
        val password: String,
        val gender: String
    ) : SignUpAction()

    data class SubmitStudent(
        val email: String,
        val password: String,
        val name: String,
        val studentID: String,
        val gender: String
    ) : SignUpAction()

    data object ClearError : SignUpAction()
    data object ResetState : SignUpAction()
}

data class SignUpUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)


