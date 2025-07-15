package com.example.studentmanagement.presentation.teacher

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

class TeacherViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BaseViewModel<TeacherAction, TeacherUiState>() {

    override fun setInitialState(): TeacherUiState = TeacherUiState()

    override fun onAction(event: TeacherAction) {
        when (event) {
            is TeacherAction.LoadTeacherData -> loadTeacherData()
            is TeacherAction.LoadStudentCounts -> loadStudentCounts()
        }
    }

    private fun loadTeacherData() {
        viewModelScope.launch {
            try {
                setState { copy(isLoading = true) }
                val userId = auth.currentUser?.uid ?: return@launch
                val teacherDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val teacherName = teacherDoc.getString("username") ?: "Teacher"
                val profileImageUrl = teacherDoc.getString("imageUrl")

                setState {
                    copy(
                        isLoading = false,
                        teacherName = teacherName,
                        profileImageUrl = profileImageUrl,
                        error = null
                    )
                }

            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun loadStudentCounts() {
        viewModelScope.launch {
            try {
                setState { copy(isLoading = true) }

                val studentsSnapshot = db.collection("students")
                    .get()
                    .await()

                val totalStudents = studentsSnapshot.size()
                var maleCount = 0
                var femaleCount = 0

                studentsSnapshot.documents.forEach { document ->
                    val gender = document.getString("gender")?.lowercase()
                    when (gender) {
                        "male", "m" -> maleCount++
                        "female", "f" -> femaleCount++
                    }
                }

                setState {
                    copy(
                        isLoading = false,
                        totalStudents = totalStudents,
                        maleStudents = maleCount,
                        femaleStudents = femaleCount,
                        error = null
                    )

                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

