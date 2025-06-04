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
            is SignUpAction.SubmitTeacher -> handleTeacherSignUp(event.email, event.password)
            is SignUpAction.SubmitStudent -> handleStudentSignUp(
                event.email,
                event.password,
                event.name,
                event.studentID
            )
        }
    }

    private fun handleTeacherSignUp(email: String, password: String) {
        viewModelScope.launch {
            if (!validateInputs(email, password)) return@launch

            setState { copy(isLoading = true, error = null) }

            signUpTeacher(email, password).fold(
                onSuccess = { setState { copy(isLoading = false, success = true) } },
                onFailure = { e ->
                    setState {
                        copy(
                            isLoading = false,
                            error = e.message ?: "Teacher registration failed"
                        )
                    }
                }
            )
        }
    }

    private fun handleStudentSignUp(
        email: String,
        password: String,
        name: String,
        studentID: String
    ) {
        viewModelScope.launch {
            if (!validateStudentInputs(email, password, name, studentID)) return@launch

            setState { copy(isLoading = true, error = null) }

            signUpStudent(email, password, name, studentID).fold(
                onSuccess = { setState { copy(isLoading = false, success = true) } },
                onFailure = { e ->
                    setState {
                        copy(
                            isLoading = false,
                            error = e.message ?: "Student registration failed"
                        )
                    }
                }
            )
        }
    }

    private suspend fun signUpTeacher(email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            firestore.collection("users").document(user.uid)
                .set(
                    mapOf(
                        "email" to email,
                        "accountType" to "teacher",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            Result.failure(e)
        }
    }

    private suspend fun signUpStudent(
        email: String,
        password: String,
        name: String,
        studentID: String
    ): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("User creation failed")

            firestore.collection("students").document(user.uid)
                .set(
                    mapOf(
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
                        "age" to null,
                        "guardian" to "",
                        "guardianContact" to "",
                        "majoring" to ""
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            auth.currentUser?.delete()?.await()
            Result.failure(e)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                setState { copy(error = "Email cannot be empty") }
                false
            }

            !isValidEmail(email) -> {
                setState { copy(error = "Invalid email format") }
                false
            }

            password.isEmpty() -> {
                setState { copy(error = "Password cannot be empty") }
                false
            }

            else -> true
        }
    }

    private fun validateStudentInputs(
        email: String,
        password: String,
        name: String,
        studentID: String
    ): Boolean {
        return when {
            !validateInputs(email, password) -> false
            name.isEmpty() -> {
                setState { copy(error = "Name cannot be empty") }
                false
            }

            studentID.isEmpty() -> {
                setState { copy(error = "ID cannot be empty") }
                false
            }

            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class SignUpAction {
    data class SubmitTeacher(val email: String, val password: String) : SignUpAction()
    data class SubmitStudent(
        val email: String,
        val password: String,
        val name: String,
        val studentID: String
    ) : SignUpAction()
}

data class SignUpUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)


