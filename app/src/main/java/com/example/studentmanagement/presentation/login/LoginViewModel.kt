package com.example.studentmanagement.presentation.login

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

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
                    verifyAccountTypeAndApproval(accountType)
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Login failed. Check credentials and try again."
                        )
                    }
                }
            }
    }

    private fun verifyAccountTypeAndApproval(requestedType: String) {
        val user = auth.currentUser ?: run {
            setState { copy(isLoading = false, error = "User not found.") }
            return
        }

        when (requestedType) {
            "student" -> verifyStudentApproval(user.uid)
            else -> verifyRegularAccountType(user.uid, requestedType)
        }
    }

    private fun verifyStudentApproval(uid: String) {
        firestore.collection("students").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Student account not found."
                        )
                    }
                    return@addOnSuccessListener
                }

                val status = doc.getString("status") ?: "inactive"
                if (status == "active") {
                    updateLastLogin(uid)
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            accountType = "student"
                        )
                    }
                } else {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Your account is pending approval from a teacher."
                        )
                    }
                }
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to verify student status."
                    )
                }
            }
    }

    private fun updateLastLogin(uid: String) {
        firestore.collection("students").document(uid)
            .update("lastLogin", FieldValue.serverTimestamp())
            .addOnFailureListener {

            }
    }

    private fun verifyRegularAccountType(uid: String, requestedType: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Teacher account not properly registered"
                        )
                    }
                    return@addOnSuccessListener
                }

                val dbType = doc.getString("accountType")
                val status = doc.getString("status") ?: "inactive"

                if (dbType == requestedType) {
                    updateTeacherLastLogin(uid)
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            accountType = dbType
                        )
                    }
                } else if (dbType != requestedType) {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Account type mismatch. Please login as $requestedType."
                        )
                    }
                } else {
                    auth.signOut()
                    setState {
                        copy(
                            isLoading = false,
                            error = "Your account is not active. Please contact administrator."
                        )
                    }
                }
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to retrieve user data."
                    )
                }
            }
    }

    private fun updateTeacherLastLogin(uid: String) {
        firestore.collection("users").document(uid)
            .update("lastLogin", FieldValue.serverTimestamp())
            .addOnFailureListener {

            }
    }

    fun errorShown() {
        setState { copy(error = null) }
    }
}

sealed class LoginAction {
    data class Login(val email: String, val password: String, val accountType: String) :
        LoginAction()
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val accountType: String? = null
)
