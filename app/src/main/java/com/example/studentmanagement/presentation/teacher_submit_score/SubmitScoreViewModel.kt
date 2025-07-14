package com.example.studentmanagement.presentation.teacher_submit_score

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class SubmitScoreViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<SubmitScoreAction, SubmitScoreUiState>() {

    private val _studentScores = MutableLiveData<List<StudentScore>>()
    val studentScores: LiveData<List<StudentScore>> = _studentScores

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    companion object {
        private const val TAG = "SubmitScoreViewModel"
    }

    override fun setInitialState(): SubmitScoreUiState = SubmitScoreUiState()

    override fun onAction(event: SubmitScoreAction) {}

    fun fetchSubjects() {
        Log.d(TAG, "Fetching subjects from Firestore")
        setState { copy(isLoading = true, error = null, submitSuccess = false) }

        firestore.collection("subjects")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Successfully fetched subjects. Count: ${result.size()}")
                val subjectList = result.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    Log.d(TAG, "Subject found: $name")
                    name
                }
                _subjects.value = subjectList
                setState { copy(isLoading = false, submitSuccess = false) }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching subjects: ${exception.message}", exception)
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
        Log.d(TAG, "Fetching students for subject: $subject")
        setState { copy(isLoading = true, error = null, submitSuccess = false) }

        firestore.collection("students")
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Successfully fetched students. Count: ${result.size()}")
                val studentIds = result.documents.map { doc ->
                    Log.d(TAG, "Student ID: ${doc.id}, Name: ${doc.getString("name")}")
                    doc.id
                }
                checkExistingScores(studentIds, subject, result)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching students: ${exception.message}", exception)
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
        studentIds: List<String>,
        subject: String,
        studentsResult: QuerySnapshot
    ) {
        Log.d(TAG, "Checking existing scores for ${studentIds.size} students, subject: $subject")

        if (studentIds.isEmpty()) {
            Log.d(TAG, "No students found")
            _studentScores.value = emptyList()
            setState { copy(isLoading = false, submitSuccess = false) }
            return
        }

        // Split studentIds into chunks of 10 (Firestore whereIn limit)
        val chunkedStudentIds = studentIds.chunked(10)
        val allScores = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
        var completedChunks = 0

        fun processChunk(chunk: List<String>) {
            Log.d(TAG, "Processing chunk of ${chunk.size} students")

            firestore.collection("scores")
                .whereIn("studentId", chunk)
                .whereEqualTo("subject", subject)
                .get()
                .addOnSuccessListener { scoresResult ->
                    Log.d(TAG, "Successfully fetched scores for chunk. Found: ${scoresResult.size()}")

                    // Add scores from this chunk
                    scoresResult.documents.forEach { doc ->
                        val studentId = doc.getString("studentId")
                        if (studentId != null) {
                            allScores[studentId] = doc
                            Log.d(TAG, "Found existing score for student: $studentId")
                        }
                    }

                    completedChunks++

                    // If all chunks are processed, create the final list
                    if (completedChunks == chunkedStudentIds.size) {
                        createStudentScoreList(allScores, studentsResult)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error fetching scores for chunk: ${exception.message}", exception)
                    setState {
                        copy(
                            isLoading = false,
                            error = "Failed to fetch scores: ${exception.message}",
                            submitSuccess = false
                        )
                    }
                }
        }

        // Process all chunks
        chunkedStudentIds.forEach { chunk ->
            processChunk(chunk)
        }
    }

    private fun createStudentScoreList(
        scoresByStudentId: Map<String, com.google.firebase.firestore.DocumentSnapshot>,
        studentsResult: QuerySnapshot
    ) {
        Log.d(TAG, "Creating student score list")

        val scores = studentsResult.documents.mapNotNull { doc ->
            val studentId = doc.id
            val studentName = doc.getString("name") ?: "Unknown"
            val existingScore = scoresByStudentId[studentId]

            Log.d(TAG, "Creating score for student: $studentName (ID: $studentId)")

            if (existingScore != null) {
                Log.d(TAG, "Found existing scores for $studentName")
            }

            StudentScore(
                studentId = studentId,
                name = studentName,
                assignment = existingScore?.getDouble("assignment")?.toFloat() ?: 0f,
                midterm = existingScore?.getDouble("midterm")?.toFloat() ?: 0f,
                final = existingScore?.getDouble("final")?.toFloat() ?: 0f,
                homework = existingScore?.getDouble("homework")?.toFloat() ?: 0f
            )
        }

        Log.d(TAG, "Created ${scores.size} student scores")
        _studentScores.value = scores
        setState { copy(isLoading = false, submitSuccess = false) }
    }

    fun submitScores(scores: List<StudentScore>, subject: String) {
        Log.d(TAG, "Submitting ${scores.size} scores for subject: $subject")

        if (!validateScores(scores)) {
            Log.w(TAG, "Score validation failed")
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
                .document("${score.studentId}_$subject")

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

            Log.d(TAG, "Adding score to batch for student: ${score.name}")
            batch.set(docRef, scoreData)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "Successfully submitted all scores")
                setState {
                    copy(
                        isLoading = false,
                        submitSuccess = true,
                        error = null
                    )
                }
                fetchStudentsBySubject(subject)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error submitting scores: ${exception.message}", exception)
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
            val isValid = score.assignment in 0f..100f &&
                    score.midterm in 0f..100f &&
                    score.final in 0f..100f &&
                    score.homework in 0f..100f

            if (!isValid) {
                Log.w(TAG, "Invalid score for student ${score.name}: " +
                        "assignment=${score.assignment}, midterm=${score.midterm}, " +
                        "final=${score.final}, homework=${score.homework}")
            }
            isValid
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
    var homework: Float = 0f
) {
    val total: Float
        get() = assignment + midterm + final + homework
}