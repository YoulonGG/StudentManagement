package com.example.studentmanagement.presentation.teacher_submit_score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SubmitScoreViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<SubmitScoreAction, SubmitScoreUiState>() {

    private val _studentScores = MutableLiveData<List<StudentScore>>()
    val studentScores: LiveData<List<StudentScore>> = _studentScores

    override fun setInitialState(): SubmitScoreUiState = SubmitScoreUiState()

    override fun onAction(event: SubmitScoreAction) {}

    fun fetchStudents(year: Int = 1, semester: Int = 1) {
        setState { copy(isLoading = true, error = null, submitSuccess = false) }
        firestore.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val studentIds = result.documents.map { it.id }
                checkExistingScores(studentIds, year, semester, result)
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch students: ${it.message}",
                        submitSuccess = false
                    )
                }
            }
    }

    private fun checkExistingScores(
        studentIds: List<String>,
        year: Int,
        semester: Int,
        studentsResult: QuerySnapshot
    ) {
        if (studentIds.isEmpty()) {
            _studentScores.value = emptyList()
            setState {
                copy(
                    isLoading = false,
                    submitSuccess = false
                )
            }
            return
        }

        firestore.collection("scores")
            .whereIn("studentId", studentIds)
            .whereEqualTo("year", year)
            .whereEqualTo("semester", semester)
            .get()
            .addOnSuccessListener { scoresResult ->
                val scoresByStudentId =
                    scoresResult.documents.associateBy { it.getString("studentId") ?: "" }

                val scores = studentsResult.map { doc ->
                    val existingScore = scoresByStudentId[doc.id]
                    StudentScore(
                        studentId = doc.id,
                        name = doc.getString("name") ?: "",
                        assignment = existingScore?.getDouble("assignment")?.toFloat() ?: 0f,
                        midterm = existingScore?.getDouble("midterm")?.toFloat() ?: 0f,
                        final = existingScore?.getDouble("final")?.toFloat() ?: 0f,
                        homework = existingScore?.getDouble("homework")?.toFloat() ?: 0f,
                        participation = existingScore?.getDouble("participation")?.toFloat() ?: 0f
                    )
                }
                _studentScores.value = scores
                setState {
                    copy(
                        isLoading = false,
                        submitSuccess = false
                    )
                }
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch scores: ${it.message}",
                        submitSuccess = false
                    )
                }
            }
    }

    fun submitScores(scores: List<StudentScore>, year: Int, semester: Int) {
        if (!validateScores(scores)) {
            setState {
                copy(
                    error = "Scores must be between 0 and 100",
                    submitSuccess = false
                )
            }
            return
        }

        setState { copy(isLoading = true, error = null, submitSuccess = false) }
        val batch = firestore.batch()

        scores.forEach { score ->
            val docRef = firestore.collection("scores")
                .document("${score.studentId}_y${year}_s${semester}")
            batch.set(
                docRef, mapOf(
                    "studentId" to score.studentId,
                    "name" to score.name,
                    "assignment" to score.assignment,
                    "midterm" to score.midterm,
                    "final" to score.final,
                    "homework" to score.homework,
                    "participation" to score.participation,
                    "total" to score.total,
                    "year" to year,
                    "semester" to semester,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.commit()
            .addOnSuccessListener {
                setState {
                    copy(
                        isLoading = false,
                        submitSuccess = true,
                        error = null
                    )
                }
                viewModelScope.launch {
                    delay(100)
                    setState { copy(submitSuccess = false) }
                    fetchStudents(year, semester)
                }
            }
            .addOnFailureListener {
                setState {
                    copy(
                        isLoading = false,
                        submitSuccess = false,
                        error = "Failed to save scores: ${it.message}"
                    )
                }
            }
    }

    private fun validateScores(scores: List<StudentScore>): Boolean {
        return scores.all { score ->
            score.assignment in 0f..100f &&
                    score.midterm in 0f..100f &&
                    score.final in 0f..100f &&
                    score.homework in 0f..100f &&
                    score.participation in 0f..100f
        }
    }
}

data class SubmitScoreUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val submitSuccess: Boolean = false
)

sealed interface SubmitScoreAction

data class StudentScore(
    val studentId: String,
    val name: String,
    var assignment: Float = 0f,
    var midterm: Float = 0f,
    var final: Float = 0f,
    var homework: Float = 0f,
    var participation: Float = 0f
) {
    val total: Float
        get() = assignment + midterm + final + homework + participation
}
