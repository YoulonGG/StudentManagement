package com.example.studentmanagement.presentation.teacher

import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _teacherData = MutableStateFlow(TeacherUiState())
    val teacherData = _teacherData.asStateFlow()

    init {
        loadTeacherData()
        loadTotalStudents()
    }

    override fun setInitialState(): TeacherUiState = TeacherUiState()

    override fun onAction(event: TeacherAction) {
        when (event) {
            is TeacherAction.LoadTeacherData -> loadTeacherData()
            is TeacherAction.LoadTotalStudents -> loadTotalStudents()
        }
    }

    private fun loadTeacherData() {
        viewModelScope.launch {
            try {
                _teacherData.update { it.copy(isLoading = true) }

                val userId = auth.currentUser?.uid ?: return@launch

                val teacherDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val teacherName = teacherDoc.getString("username") ?: "Teacher"
                val profileImageUrl = teacherDoc.getString("imageUrl")

                _teacherData.update { it.copy(
                    isLoading = false,
                    teacherName = teacherName,
                    profileImageUrl = profileImageUrl,
                    error = null
                ) }
            } catch (e: Exception) {
                _teacherData.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }
    private fun loadTotalStudents() {
        viewModelScope.launch {
            try {
                _teacherData.update { it.copy(isLoading = true) }

                val studentsSnapshot = db.collection("users")
                    .whereEqualTo("role", "student")
                    .get()
                    .await()

                val totalStudents = studentsSnapshot.size()

                _teacherData.update {
                    it.copy(
                        isLoading = false,
                        totalStudents = totalStudents,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _teacherData.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

data class TeacherUiState(
    val isLoading: Boolean = false,
    val teacherName: String = "",
    val totalStudents: Int = 0,
    val profileImageUrl: String? = null,
    val error: String? = null
)

sealed interface TeacherAction {
    object LoadTeacherData : TeacherAction
    object LoadTotalStudents : TeacherAction
}
