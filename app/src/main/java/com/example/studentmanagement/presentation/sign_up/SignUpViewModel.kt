package com.example.studentmanagement.presentation.sign_up

import android.util.Patterns
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
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

    private fun handleTeacherSignUp(
        email: String,
        password: String,
        gender: String,
        username: String
    ) {
        viewModelScope.launch {
            try {
                if (!validateTeacherInputs(email, password, gender)) return@launch
                setState { copy(isLoading = true, error = null) }

                val result = signUpTeacher(email, password, gender, username)
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

    override fun onAction(event: SignUpAction) {
        when (event) {
            is SignUpAction.SubmitTeacher -> handleTeacherSignUp(
                email = event.email,
                password = event.password,
                gender = event.gender,
                username = event.username
            )

            SignUpAction.ClearError -> setState { copy(error = null) }
            SignUpAction.ResetState -> setState { SignUpUiState() }
        }
    }

    private suspend fun signUpTeacher(
        email: String,
        password: String,
        gender: String,
        username: String
    ): Result<Unit> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("User creation failed")

        val teacherData = hashMapOf(
            "email" to email,
            "accountType" to "teacher",
            "password" to password,
            "username" to username,
            "gender" to gender,
            "authUid" to user.uid,
            "imageUrl" to "",
        )

        firestore.collection("users").document(user.uid)
            .set(teacherData)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        auth.currentUser?.delete()?.await()
        Result.failure(Exception("Teacher registration failed: ${e.message}"))
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

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}




