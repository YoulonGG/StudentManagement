package com.example.studentmanagement.presentation.teacher_profile

import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TeacherProfileViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BaseViewModel<TeacherProfileAction, TeacherProfileUiState>() {
    override fun setInitialState(): TeacherProfileUiState = TeacherProfileUiState()

    override fun onAction(event: TeacherProfileAction) {
        when (event) {

            else -> {}
        }
    }

}


data class TeacherProfileUiState(
    val isLoading: Boolean = true
)


sealed interface TeacherProfileAction {}