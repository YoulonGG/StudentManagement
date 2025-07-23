package com.example.studentmanagement.presentation.login

import android.util.Log
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BaseViewModel<LoginAction, LoginUiState>() {

    override fun setInitialState(): LoginUiState = LoginUiState()

    override fun onAction(event: LoginAction) {
        when (event) {
            is LoginAction.Login -> login(event.email, event.password, event.accountType)
        }
    }

    private fun login(email: String, password: String, accountType: String) {
        setState { copy(isLoading = true, error = null) }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    validateAccountType(accountType)
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = task.exception?.message
                                ?: "Login failed. Check credentials and try again."
                        )
                    }
                }
            }
    }

    private fun validateAccountType(expectedAccountType: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            setState {
                copy(
                    isLoading = false,
                    error = "Authentication failed. Please try again."
                )
            }
            return
        }

        val collectionName = if (expectedAccountType.equals("student", ignoreCase = true)) {
            "students"
        } else {
            "users"
        }

        Log.d("LoginViewModel", "Checking collection: $collectionName for account type: $expectedAccountType")

        firestore.collection(collectionName)
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                Log.d("LoginViewModel", "Document exists: ${document.exists()}")

                if (document.exists()) {
                    val userAccountType = if (collectionName == "student") {
                        "student"
                    } else {
                        document.getString("accountType")
                    }

                    if (userAccountType != null) {
                        if (userAccountType.equals(expectedAccountType, ignoreCase = true)) {
                            setState {
                                copy(
                                    isLoading = false,
                                    success = true,
                                    accountType = expectedAccountType
                                )
                            }
                        } else {
                            auth.signOut()
                            val errorMessage =
                                if (expectedAccountType.equals("teacher", ignoreCase = true)) {
                                    "Access denied. This account is registered as a student."
                                } else {
                                    "Access denied. This account is registered as a teacher."
                                }

                            setState {
                                copy(
                                    isLoading = false,
                                    error = errorMessage
                                )
                            }
                        }
                    } else {
                        auth.signOut()
                        setState {
                            copy(
                                isLoading = false,
                                error = "Account profile incomplete. Please contact system administrator."
                            )
                        }
                    }
                } else {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Account profile not found. Please contact system administrator."
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginViewModel", "Firestore error: ${exception.message}", exception)
                auth.signOut()

                val errorMessage = when (exception) {
                    is FirebaseFirestoreException -> {
                        when (exception.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                "Access denied. Please check your permissions."
                            FirebaseFirestoreException.Code.UNAVAILABLE ->
                                "Service temporarily unavailable. Please try again."
                            FirebaseFirestoreException.Code.UNAUTHENTICATED ->
                                "Authentication expired. Please try again."
                            else -> "Database error: ${exception.message}"
                        }
                    }
                    else -> "Unable to verify account credentials. Please try again or contact system administrator."
                }

                setState {
                    copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
    }

    fun errorShown() {
        setState { copy(error = null) }
    }
}