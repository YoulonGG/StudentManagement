package com.example.studentmanagement.presentation.sign_up

import android.util.Log
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

class SignUpViewModel : BaseViewModel<SignUpAction, SignUpState>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
        return suspendCancellableCoroutine { cont ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userData = hashMapOf(
                            "email" to email,
                            "accountType" to "teacher"
                        )
                        user?.uid?.let { uid ->
                            db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    cont.resume(Result.success(Unit)) {}
                                }
                                .addOnFailureListener { e ->
                                    cont.resume(Result.failure(e)) {}
                                }
                        } ?: run {
                            cont.resume(Result.failure(Exception("User UID is null"))) {}
                        }
                    } else {
                        cont.resume(Result.failure(task.exception ?: Exception("Signup failed"))) {}
                    }
                }
        }
    }


    override suspend fun handleIntent(intent: SignUpAction) {
        Log.e("SignUpViewModel", "handleIntent called with $intent")
        when (intent) {
            is SignUpAction.Submit -> {
                updateState(SignUpState.Loading)
                val email = intent.email.trim()
                val password = intent.password.trim()

                if (email.isEmpty()) {
                    updateState(SignUpState.Error("Email cannot be empty"))
                    return
                }
                if (!isValidEmail(email)) {
                    updateState(SignUpState.Error("Invalid email format"))
                    return
                }
                if (password.isEmpty()) {
                    updateState(SignUpState.Error("Password cannot be empty"))
                    return
                }

                try {
                    val result = signUpWithEmail(email, password)
                    if (result.isSuccess) {
                        updateState(SignUpState.Success)
                    } else {
                        updateState(
                            SignUpState.Error(
                                result.exceptionOrNull()?.message ?: "Unknown error"
                            )
                        )
                    }
                } catch (e: Exception) {
                    updateState(SignUpState.Error("Signup failed: ${e.message}"))
                }
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email.matches(emailRegex.toRegex())
    }
}

