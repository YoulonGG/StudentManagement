package com.example.studentmanagement.presentation.student_score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.FirebaseFirestore

class StudentScoreViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<StudentScoreViewAction, StudentScoreViewUiState>() {

    private val _studentScores = MutableLiveData<List<StudentScoreDetail>>()
    val studentScores: LiveData<List<StudentScoreDetail>> = _studentScores

    private val _studentInfo = MutableLiveData<StudentInfo>()
    val studentInfo: LiveData<StudentInfo> = _studentInfo

    override fun setInitialState(): StudentScoreViewUiState = StudentScoreViewUiState()

    override fun onAction(event: StudentScoreViewAction) {
        when (event) {
            is StudentScoreViewAction.LoadStudentScores -> loadStudentScores(event.studentId)
            is StudentScoreViewAction.RefreshScores -> refreshScores(event.studentId)
        }
    }

    private fun loadStudentScores(studentId: String) {
        setState { copy(isLoading = true, error = null) }

        // First, get student info
        firestore.collection("students")
            .document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val studentInfo = StudentInfo(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        studentId = document.getString("studentId") ?: ""
                    )
                    _studentInfo.value = studentInfo

                    fetchScores(studentId)
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Student not found"
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load student info: ${exception.message}"
                    )
                }
            }
    }

    private fun fetchScores(studentId: String) {
        firestore.collection("scores")
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { result ->
                val scores = result.documents.mapNotNull { doc ->
                    try {
                        StudentScoreDetail(
                            subject = doc.getString("subject") ?: "",
                            assignment = doc.getDouble("assignment")?.toFloat() ?: 0f,
                            homework = doc.getDouble("homework")?.toFloat() ?: 0f,
                            midterm = doc.getDouble("midterm")?.toFloat() ?: 0f,
                            final = doc.getDouble("final")?.toFloat() ?: 0f,
                            total = doc.getDouble("total")?.toFloat() ?: 0f,
                            timestamp = doc.getTimestamp("timestamp")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.subject }

                _studentScores.value = scores
                setState { copy(isLoading = false) }
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load scores: ${exception.message}"
                    )
                }
            }
    }

    private fun refreshScores(studentId: String) {
        loadStudentScores(studentId)
    }

    fun calculateOverallGPA(scores: List<StudentScoreDetail>): Float {
        if (scores.isEmpty()) return 0f

        val totalPercentage = scores.map { (it.total / 400) * 100 }.average()
        return when {
            totalPercentage >= 90 -> 4.0f
            totalPercentage >= 80 -> 3.0f
            totalPercentage >= 70 -> 2.0f
            totalPercentage >= 60 -> 1.0f
            else -> 0.0f
        }
    }
//
//    fun getGradeForScore(percentage: Float): String {
//        return when {
//            percentage >= 90 -> "A"
//            percentage >= 80 -> "B"
//            percentage >= 70 -> "C"
//            percentage >= 60 -> "D"
//            else -> "F"
//        }
//    }
}

