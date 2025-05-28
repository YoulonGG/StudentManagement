package com.example.studentmanagement.presentation.login

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */

class LoginViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : BaseViewModel<LoginAction, LoginUiState>() {


    fun start() {
        updateState(LoginUiState.Idle)
        startIntentCollector()
    }

    override suspend fun handleIntent(intent: LoginAction) {
        when (intent) {
            is LoginAction.Login -> login(intent.email, intent.password, intent.accountType)
        }
    }

    private fun login(email: String, password: String, accountType: String) {
        updateState(LoginUiState.Loading)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        firestore.collection("users").document(user.uid).get()
                            .addOnSuccessListener { doc ->
                                val dbType = doc.getString("accountType")
                                if (dbType == accountType) {
                                    updateState(LoginUiState.Success(accountType))
                                } else {
                                    auth.signOut()
                                    updateState(LoginUiState.Error("Account type mismatch. Please login as $accountType."))
                                }
                            }
                            .addOnFailureListener {
                                updateState(LoginUiState.Error("Failed to retrieve user data."))
                            }
                    } else {
                        updateState(LoginUiState.Error("User not found."))
                    }
                } else {
                    updateState(LoginUiState.Error("Login failed. Check credentials and try again."))
                }
            }
    }
}
