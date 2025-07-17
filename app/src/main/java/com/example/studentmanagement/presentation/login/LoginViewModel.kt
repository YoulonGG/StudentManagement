package com.example.studentmanagement.presentation.login

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userAccountType = document.getString("accountType")
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
            .addOnFailureListener {
                auth.signOut()
                setState {
                    copy(
                        isLoading = false,
                        error = "Unable to verify account credentials. Please try again or contact system administrator."
                    )
                }
            }
    }

    fun errorShown() {
        setState { copy(error = null) }
    }
}