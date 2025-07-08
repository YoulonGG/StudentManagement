package com.example.studentmanagement.presentation.student_score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore

class StudentScoreViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<StudentScoreAction, StudentScoreUiState>() {

    private val _studentScores = MutableLiveData<List<ScoreItem>>()
    val studentScores: LiveData<List<ScoreItem>> = _studentScores

    override fun setInitialState(): StudentScoreUiState = StudentScoreUiState()

    override fun onAction(event: StudentScoreAction) {}

    fun fetchScores(studentId: String) {
        setState { copy(isLoading = true, error = null) }
        firestore.collection("scores")
            .whereEqualTo("studentId", studentId)
            .orderBy("year")
            .orderBy("semester")
            .get()
            .addOnSuccessListener { result ->
                val scores = result.documents.map { doc ->
                    ScoreItem(
                        year = doc.getLong("year")?.toInt() ?: 0,
                        semester = doc.getLong("semester")?.toInt() ?: 0,
                        assignment = doc.getDouble("assignment")?.toFloat() ?: 0f,
                        midterm = doc.getDouble("midterm")?.toFloat() ?: 0f,
                        final = doc.getDouble("final")?.toFloat() ?: 0f,
                        homework = doc.getDouble("homework")?.toFloat() ?: 0f,
                        participation = doc.getDouble("participation")?.toFloat() ?: 0f,
                        total = doc.getDouble("total")?.toFloat() ?: 0f
                    )
                }
                _studentScores.value = scores
                setState { copy(isLoading = false) }
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch scores: ${it.message}"
                    )
                }
            }
    }
}

data class StudentScoreUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface StudentScoreAction

data class ScoreItem(
    val year: Int,
    val semester: Int,
    val assignment: Float,
    val midterm: Float,
    val final: Float,
    val homework: Float,
    val participation: Float,
    val total: Float
)