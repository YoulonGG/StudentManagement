package com.example.studentmanagement.presentation.teacher_attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.core.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author: John Youlong.
 * @Date: 6/10/25.
 * @Email: johnyoulong@gmail.com.
 */

class TeacherAttendanceViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BaseViewModel<TeacherAttendanceEvent, TeacherAttendanceState>() {

    val attendanceAdapter = AttendanceAdapter { studentId, status ->
        onAction(TeacherAttendanceEvent.UpdateStatus(studentId, status))
    }

    override fun setInitialState(): TeacherAttendanceState = TeacherAttendanceState()

    override fun onAction(event: TeacherAttendanceEvent) {
        when (event) {
            is TeacherAttendanceEvent.LoadStudents -> {
                loadStudents()
            }

            is TeacherAttendanceEvent.UpdateStatus -> {
                updateStatus(event.studentId, event.status)
            }

            is TeacherAttendanceEvent.SubmitAttendance -> {
                submitAttendance()
            }

            is TeacherAttendanceEvent.SetDate -> {
                setState { copy(selectedDate = event.date) }
                checkAttendanceExists(event.date)
            }

            TeacherAttendanceEvent.ClearError -> {
                setState { copy(error = null) }
            }

            TeacherAttendanceEvent.ClearSuccess -> {
                setState { copy(submissionSuccess = false) }
            }
        }
    }

    private fun loadStudents() {
        setState { copy(isLoading = true) }

        db.collection("students")
            .get()
            .addOnSuccessListener { snapshot ->
                val students = snapshot.documents.map { doc ->
                    AttendanceStatus(
                        studentId = doc.id,
                        fullName = doc.getString("name") ?: "Unknown"
                    )
                }
                setState {
                    copy(
                        students = students,
                        isLoading = false,
                        error = null
                    )
                }
                attendanceAdapter.updateList(students)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load: ${e.message}"
                    )
                }
            }
    }

    private fun updateStatus(studentId: String, status: String) {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }

        val updatedStudents = uiState.value.students.map {
            if (it.studentId == studentId) it.copy(status = status) else it
        }
        setState { copy(students = updatedStudents) }
        attendanceAdapter.updateList(updatedStudents)
    }


    private fun submitAttendance() {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }
        val currentUser = auth.currentUser
        if (currentUser == null) {
            setState { copy(error = "You must be logged in to submit attendance") }
            return
        }

        setState { copy(isSubmitting = true, error = null) }

        // First verify the user is a teacher
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("accountType") == "teacher") {
                    proceedWithSubmission(currentUser.uid)
                } else {
                    setState {
                        copy(
                            isSubmitting = false,
                            error = "Only teachers can submit attendance"
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isSubmitting = false,
                        error = "Authentication failed: ${e.message}"
                    )
                }
            }
    }

    private fun proceedWithSubmission(teacherId: String) {
        val batch = db.batch()

        // Create the main attendance document
        val attendanceRef = db.collection("attendance").document(uiState.value.selectedDate)
        batch.set(attendanceRef, hashMapOf(
            "date" to uiState.value.selectedDate,  // Add the date field
            "teacherId" to teacherId,
            "timestamp" to FieldValue.serverTimestamp()
        ))

        // Add attendance records for each student
        uiState.value.students.forEach { student ->
            val studentRef = attendanceRef.collection("attendance_records").document(student.studentId)
            batch.set(
                studentRef, hashMapOf(
                    "status" to student.status,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "teacherId" to teacherId
                )
            )
        }

        batch.commit()
            .addOnSuccessListener {
                val updatedStudents = uiState.value.students.map { it.copy(isSubmitted = true) }
                setState {
                    copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        attendanceSubmitted = true,
                        students = updatedStudents
                    )
                }
                attendanceAdapter.updateList(updatedStudents)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isSubmitting = false,
                        error = "Submission failed: ${e.message}"
                    )
                }
            }
    }
    private fun checkAttendanceExists(date: String) {
        setState { copy(isLoading = true) }

        db.collection("attendance")
            .document(date)
            .collection("attendance_records")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                setState {
                    copy(
                        isLoading = false,
                        attendanceSubmitted = !snapshot.isEmpty
                    )
                }
                if (!snapshot.isEmpty) {
                    loadSubmittedAttendance(date)
                } else {
                    loadStudents()
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Error checking attendance: ${e.message}"
                    )
                }
            }
    }


    private fun loadSubmittedAttendance(date: String) {
        setState { copy(isLoading = true) }

        db.collection("attendance")
            .document(date)
            .collection("attendance_records")
            .get()
            .addOnSuccessListener { snapshot ->
                val submittedRecords = snapshot.documents.map { doc ->
                    doc.id to doc.getString("status")
                }.toMap()

                db.collection("students")
                    .get()
                    .addOnSuccessListener { studentsSnapshot ->
                        val students = studentsSnapshot.documents.map { doc ->
                            AttendanceStatus(
                                studentId = doc.id,
                                fullName = doc.getString("name") ?: "Unknown",
                                status = submittedRecords[doc.id] ?: "Present",
                                isSubmitted = true  // Mark as submitted
                            )
                        }
                        setState {
                            copy(
                                students = students,
                                isLoading = false,
                                error = null,
                                attendanceSubmitted = true
                            )
                        }
                        attendanceAdapter.updateList(students)
                    }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load submitted attendance: ${e.message}"
                    )
                }
            }
    }


}

class AttendanceAdapter(
    private val onStatusChanged: (String, String) -> Unit
) : ListAdapter<AttendanceStatus, AttendanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<AttendanceStatus>) {
        submitList(newList)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewStudentName: TextView = view.findViewById(R.id.textViewStudentName)
        private val radioGroupStatus: RadioGroup = view.findViewById(R.id.radioGroupStatus)
        private val radioPresent: RadioButton = view.findViewById(R.id.radioPresent)
        private val radioAbsent: RadioButton = view.findViewById(R.id.radioAbsent)
        private val radioLate: RadioButton = view.findViewById(R.id.radioLate)

        fun bind(item: AttendanceStatus) {
            textViewStudentName.text = item.fullName

            radioGroupStatus.setOnCheckedChangeListener(null)

            when (item.status) {
                "Present" -> radioPresent.isChecked = true
                "Absent" -> radioAbsent.isChecked = true
                "Late" -> radioLate.isChecked = true
            }

            setRadioButtonsEnabled(!item.isSubmitted)

            if (!item.isSubmitted) {
                radioGroupStatus.setOnCheckedChangeListener { _, checkedId ->
                    val status = when (checkedId) {
                        R.id.radioPresent -> "Present"
                        R.id.radioAbsent -> "Absent"
                        R.id.radioLate -> "Late"
                        else -> "Present"
                    }
                    onStatusChanged(item.studentId, status)
                }
            }
        }

        private fun setRadioButtonsEnabled(enabled: Boolean) {
            radioPresent.isEnabled = enabled
            radioAbsent.isEnabled = enabled
            radioLate.isEnabled = enabled
            radioGroupStatus.alpha = if (enabled) 1.0f else 0.6f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AttendanceStatus>() {
        override fun areItemsTheSame(oldItem: AttendanceStatus, newItem: AttendanceStatus) =
            oldItem.studentId == newItem.studentId

        override fun areContentsTheSame(oldItem: AttendanceStatus, newItem: AttendanceStatus) =
            oldItem == newItem
    }
}

data class TeacherAttendanceState(
    val students: List<AttendanceStatus> = emptyList(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val attendanceSubmitted: Boolean = false
)

sealed class TeacherAttendanceEvent {
    data object LoadStudents : TeacherAttendanceEvent()
    data class UpdateStatus(val studentId: String, val status: String) : TeacherAttendanceEvent()
    data object SubmitAttendance : TeacherAttendanceEvent()
    data class SetDate(val date: String) : TeacherAttendanceEvent()
    data object ClearError : TeacherAttendanceEvent()
    data object ClearSuccess : TeacherAttendanceEvent()

}

data class AttendanceStatus(
    val studentId: String,
    val fullName: String,
    val status: String = "Present",
    val isSubmitted: Boolean = false
)
