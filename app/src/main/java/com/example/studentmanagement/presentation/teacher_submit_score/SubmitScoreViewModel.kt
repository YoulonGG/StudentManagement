package com.example.studentmanagement.presentation.teacher_submit_score

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.DocumentSnapshot
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

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects


    override fun setInitialState(): SubmitScoreUiState = SubmitScoreUiState()

    override fun onAction(event: SubmitScoreAction) {}

    fun fetchSubjects() {
        setState { copy(isLoading = true, error = null, submitSuccess = false) }
        firestore.collection("subjects")
            .get()
            .addOnSuccessListener { result ->
                val subjectList = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    return@mapNotNull name
                }
                _subjects.value = subjectList
                setState { copy(isLoading = false, submitSuccess = false) }
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch subjects: ${exception.message}",
                        submitSuccess = false
                    )
                }
            }
    }

    fun fetchStudentsBySubject(subject: String) {
        setState { copy(isLoading = true, error = null, submitSuccess = false) }
        firestore.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val studentIds = result.documents.map { doc ->
                    doc.id
                }
                checkExistingScores(studentIds, subject, result)
            }
            .addOnFailureListener { exception ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch students: ${exception.message}",
                        submitSuccess = false
                    )
                }
            }
    }

    private fun checkExistingScores(
        studentIds: List<String>, subject: String, studentsResult: QuerySnapshot
    ) {
        if (studentIds.isEmpty()) {
            _studentScores.value = emptyList()
            setState { copy(isLoading = false, submitSuccess = false) }
            return
        }

        val chunkedStudentIds = studentIds.chunked(10)
        val allScores = mutableMapOf<String, DocumentSnapshot>()
        var completedChunks = 0

        fun processChunk(chunk: List<String>) {
            firestore.collection("scores").whereIn("studentId", chunk)
                .whereEqualTo("subject", subject).get().addOnSuccessListener { scoresResult ->
                    scoresResult.documents.forEach { doc ->
                        val studentId = doc.getString("studentId")
                        if (studentId != null) {
                            allScores[studentId] = doc
                        }
                    }

                    completedChunks++

                    if (completedChunks == chunkedStudentIds.size) {
                        createStudentScoreList(allScores, studentsResult)
                    }
                }.addOnFailureListener { exception ->
                    setState {
                        copy(
                            isLoading = false,
                            error = "Failed to fetch scores: ${exception.message}",
                            submitSuccess = false
                        )
                    }
                }
        }

        chunkedStudentIds.forEach { chunk ->
            processChunk(chunk)
        }
    }

    private fun createStudentScoreList(
        scoresByStudentId: Map<String, DocumentSnapshot>,
        studentsResult: QuerySnapshot
    ) {
        val scores = studentsResult.documents.mapNotNull { doc ->
            val studentId = doc.id
            val studentName = doc.getString("name") ?: "Unknown"
            val existingScore = scoresByStudentId[studentId]

            StudentScore(
                studentId = studentId,
                name = studentName,
                assignment = existingScore?.getDouble("assignment")?.toFloat() ?: 0f,
                midterm = existingScore?.getDouble("midterm")?.toFloat() ?: 0f,
                final = existingScore?.getDouble("final")?.toFloat() ?: 0f,
                homework = existingScore?.getDouble("homework")?.toFloat() ?: 0f
            )
        }

        _studentScores.value = scores.toList()
        setState { copy(isLoading = false, submitSuccess = false) }
    }

    fun submitScores(scores: List<StudentScore>, subject: String) {
        if (!validateScores(scores)) {
            setState {
                copy(
                    error = "Scores must be between 0 and 100", submitSuccess = false
                )
            }
            return
        }

        setState { copy(isLoading = true, error = null, submitSuccess = false) }
        val batch = firestore.batch()

        scores.forEach { score ->
            val docRef = firestore.collection("scores").document("${score.studentId}_$subject")

            val scoreData = mapOf(
                "studentId" to score.studentId,
                "name" to score.name,
                "assignment" to score.assignment,
                "midterm" to score.midterm,
                "final" to score.final,
                "homework" to score.homework,
                "total" to score.total,
                "subject" to subject,
                "timestamp" to FieldValue.serverTimestamp()
            )

            batch.set(docRef, scoreData)
        }

        batch.commit().addOnSuccessListener {
            setState {
                copy(
                    isLoading = false, submitSuccess = true, error = null
                )
            }
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                delay(500)
                fetchStudentsBySubject(subject)
            }
        }.addOnFailureListener { exception ->
            setState {
                copy(
                    isLoading = false,
                    submitSuccess = false,
                    error = "Failed to save scores: ${exception.message}"
                )
            }
        }
    }

    private fun validateScores(scores: List<StudentScore>): Boolean {
        return scores.all { score ->
            val isValid =
                score.assignment in 0f..100f && score.midterm in 0f..100f && score.final in 0f..100f && score.homework in 0f..100f
            return isValid
        }
    }
}