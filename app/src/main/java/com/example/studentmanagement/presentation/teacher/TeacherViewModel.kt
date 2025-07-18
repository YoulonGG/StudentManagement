package com.example.studentmanagement.presentation.teacher

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.TeacherResponse
import com.example.studentmanagement.data.local.PreferencesKeys
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
    private val auth: FirebaseAuth,
    private val sharedPreferences: SharedPreferences
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
                val userId = auth.currentUser?.uid ?: run {
                    setState { copy(error = "User not authenticated") }
                    return@launch
                }

                val cachedTeacher = loadTeacherFromSharedPrefs()
                if (cachedTeacher != null) {
                    setState {
                        copy(
                            teacherName = cachedTeacher.username ?: "Unknown Teacher",
                            profileImageUrl = cachedTeacher.imageUrl,
                            isLoading = false
                        )
                    }
                    return@launch
                }

                setState { copy(isLoading = true) }

                val teacherDoc = db.collection("users")
                    .document(userId)
                    .get()
                    .await()
                    .also { println("DEBUG: Firestore document: ${it.data}") }

                if (!teacherDoc.exists()) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Teacher document not found"
                        )
                    }
                    return@launch
                }

                val teacherName = teacherDoc.getString("username") ?: run {
                    setState { copy(error = "Username field missing") }
                    return@launch
                }

                val profileImageUrl = teacherDoc.getString("imageUrl")

                saveTeacherToSharedPrefs(teacherName, profileImageUrl)
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
                        error = "Error loading data: ${e.localizedMessage}"
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

    private fun saveTeacherToSharedPrefs(username: String, imageUrl: String?) {
        with(sharedPreferences.edit()) {
            putString(PreferencesKeys.TEACHER_USERNAME, username)
            putString(PreferencesKeys.TEACHER_IMAGE_URL, imageUrl)
            apply()
        }
    }

    private fun loadTeacherFromSharedPrefs(): TeacherResponse? {
        val username = sharedPreferences.getString(PreferencesKeys.TEACHER_USERNAME, null)
        val email = sharedPreferences.getString(PreferencesKeys.TEACHER_EMAIL, null)

        if (username == null || email == null) {
            return null
        }

        return TeacherResponse(
            accountType = "teacher",
            username = username,
            imageUrl = sharedPreferences.getString(PreferencesKeys.TEACHER_IMAGE_URL, null)
        )
    }
}
