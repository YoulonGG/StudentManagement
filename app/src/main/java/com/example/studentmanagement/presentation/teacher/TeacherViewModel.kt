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
        loadStudentCounts()
    }

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
                _teacherData.update { it.copy(isLoading = true) }

                val userId = auth.currentUser?.uid ?: return@launch

                val teacherDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val teacherName = teacherDoc.getString("username") ?: "Teacher"
                val profileImageUrl = teacherDoc.getString("imageUrl")

                _teacherData.update {
                    it.copy(
                        isLoading = false,
                        teacherName = teacherName,
                        profileImageUrl = profileImageUrl,
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

    private fun loadStudentCounts() {
        viewModelScope.launch {
            try {
                _teacherData.update { it.copy(isLoading = true) }

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

                _teacherData.update {
                    it.copy(
                        isLoading = false,
                        totalStudents = totalStudents,
                        maleStudents = maleCount,
                        femaleStudents = femaleCount,
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
    val maleStudents: Int = 0,
    val femaleStudents: Int = 0,
    val profileImageUrl: String? = null,
    val error: String? = null
)

sealed interface TeacherAction {
    data object LoadTeacherData : TeacherAction
    data object LoadStudentCounts : TeacherAction
}