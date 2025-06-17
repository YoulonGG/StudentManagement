package com.example.studentmanagement.presentation.teacher_attendance

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.presentation.ask_permission.PermissionStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private val db: FirebaseFirestore, private val auth: FirebaseAuth
) : BaseViewModel<TeacherAttendanceEvent, TeacherAttendanceState>() {

    val attendanceAdapter = AttendanceAdapter(onStatusChanged = { studentId, status ->
        onAction(TeacherAttendanceEvent.UpdateStatus(studentId, status))
    }, onPermissionAction = { studentId, approved ->
        onAction(
            if (approved) TeacherAttendanceEvent.ApprovePermission(studentId)
            else TeacherAttendanceEvent.RejectPermission(studentId)
        )
    })

    override fun setInitialState(): TeacherAttendanceState = TeacherAttendanceState()


    override fun onAction(event: TeacherAttendanceEvent) {
        when (event) {
            is TeacherAttendanceEvent.LoadStudents -> loadStudents()
            is TeacherAttendanceEvent.UpdateStatus -> updateStatus(event.studentId, event.status)
            is TeacherAttendanceEvent.SubmitAttendance -> submitAttendance()
            is TeacherAttendanceEvent.SetDate -> {
                setState { copy(selectedDate = event.date) }
                checkAttendanceExists(event.date)
            }

            TeacherAttendanceEvent.ClearError -> setState { copy(error = null) }
            TeacherAttendanceEvent.ClearSuccess -> setState { copy(submissionSuccess = false) }
            is TeacherAttendanceEvent.ApprovePermission -> handlePermission(event.studentId, true)
            is TeacherAttendanceEvent.RejectPermission -> handlePermission(event.studentId, false)
        }
    }

    private fun loadStudents() {
        setState { copy(isLoading = true) }

        // Load all permission requests for the date, not just pending ones
        db.collection("permission_requests")
            .whereEqualTo("date", uiState.value.selectedDate)
            .get()
            .addOnSuccessListener { permissionSnapshot ->
                val permissionRequests = permissionSnapshot.documents.associate { doc ->
                    doc.getString("studentId")!! to PermissionRequestInfo(
                        reason = doc.getString("reason") ?: "",
                        status = doc.getString("status")?.let {
                            PermissionStatus.valueOf(it)
                        } ?: PermissionStatus.PENDING
                    )
                }

                // Then load students with their permission status
                db.collection("students").get()
                    .addOnSuccessListener { snapshot ->
                        val students = snapshot.documents.map { doc ->
                            val permissionInfo = permissionRequests[doc.id]
                            StudentAttendance(
                                studentId = doc.id,
                                fullName = doc.getString("name") ?: "Unknown",
                                status = when (permissionInfo?.status) {
                                    PermissionStatus.APPROVED -> AttendanceStatus.PERMISSION
                                    PermissionStatus.REJECTED -> AttendanceStatus.ABSENT
                                    else -> AttendanceStatus.PRESENT
                                },
                                hasPermissionRequest = permissionInfo?.status == PermissionStatus.PENDING,
                                permissionReason = permissionInfo?.reason,
                                date = uiState.value.selectedDate
                            )
                        }
                        setState {
                            copy(
                                students = students,
                                isLoading = false,
                                error = null
                            )
                        }
                        attendanceAdapter.submitList(students)
                    }
            }
    }

    private data class PermissionRequestInfo(
        val reason: String,
        val status: PermissionStatus
    )

    private fun handlePermission(studentId: String, approved: Boolean) {
        setState { copy(isLoading = true) }

        db.collection("permission_requests")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("date", uiState.value.selectedDate)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val document = snapshot.documents.first()
                    document.reference.update(
                        mapOf(
                            "status" to if (approved) PermissionStatus.APPROVED.name
                            else PermissionStatus.REJECTED.name,
                            "responseTimestamp" to FieldValue.serverTimestamp()
                        )
                    ).addOnSuccessListener {
                        val updatedStudents = uiState.value.students.map {
                            if (it.studentId == studentId) {
                                it.copy(
                                    hasPermissionRequest = false,
                                    status = if (approved) AttendanceStatus.PERMISSION
                                    else AttendanceStatus.ABSENT,
                                    isSubmitted = true  // Lock the status after decision
                                )
                            } else it
                        }
                        setState {
                            copy(
                                students = updatedStudents,
                                isLoading = false
                            )
                        }
                        attendanceAdapter.submitList(updatedStudents)
                    }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to update permission: ${e.message}"
                    )
                }
            }
    }

    private fun updateStatus(studentId: String, status: AttendanceStatus) {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }

        val student = uiState.value.students.find { it.studentId == studentId }
        if (student?.status == AttendanceStatus.PERMISSION) {
            setState { copy(error = "Cannot change status of approved permission") }
            return
        }

        val updatedStudents = uiState.value.students.map {
            if (it.studentId == studentId) it.copy(status = status) else it
        }
        setState { copy(students = updatedStudents) }
        attendanceAdapter.submitList(updatedStudents)
    }


    private fun submitAttendance() {
        if (uiState.value.attendanceSubmitted) {
            setState { copy(error = "Attendance has already been submitted for this date") }
            return
        }

        val currentUser = auth.currentUser ?: run {
            setState { copy(error = "You must be logged in to submit attendance") }
            return
        }

        setState { copy(isSubmitting = true, error = null) }

        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            if (document.exists() && document.getString("accountType") == "teacher") {
                proceedWithSubmission(currentUser.uid)
            } else {
                setState {
                    copy(
                        isSubmitting = false, error = "Only teachers can submit attendance"
                    )
                }
            }
        }.addOnFailureListener { e ->
            setState {
                copy(
                    isSubmitting = false, error = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    private fun proceedWithSubmission(teacherId: String) {
        val batch = db.batch()
        val attendanceRef = db.collection("attendance").document(uiState.value.selectedDate)

        batch.set(
            attendanceRef, hashMapOf(
                "date" to uiState.value.selectedDate,
                "teacherId" to teacherId,
                "timestamp" to FieldValue.serverTimestamp()
            )
        )

        uiState.value.students.forEach { student ->
            val studentRef =
                attendanceRef.collection("attendance_records").document(student.studentId)
            batch.set(
                studentRef, hashMapOf(
                    "status" to student.status.name,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "teacherId" to teacherId
                )
            )
        }

        batch.commit()
            .addOnSuccessListener {
                val updatedStudents = uiState.value.students.map {
                    it.copy(
                        isSubmitted = true,
                        status = it.status
                    )
                }
                setState {
                    copy(
                        isSubmitting = false,
                        submissionSuccess = true,
                        attendanceSubmitted = true,
                        students = updatedStudents
                    )
                }
                attendanceAdapter.submitList(updatedStudents)
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

        db.collection("attendance").document(date).collection("attendance_records").limit(1).get()
            .addOnSuccessListener { snapshot ->
                setState {
                    copy(
                        isLoading = false, attendanceSubmitted = !snapshot.isEmpty
                    )
                }
                if (!snapshot.isEmpty) {
                    loadSubmittedAttendance(date)
                } else {
                    loadStudents()
                }
            }.addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false, error = "Error checking attendance: ${e.message}"
                    )
                }
            }
    }

    private fun loadSubmittedAttendance(date: String) {
        setState { copy(isLoading = true) }

        db.collection("attendance").document(date).collection("attendance_records").get()
            .addOnSuccessListener { snapshot ->
                val submittedRecords = snapshot.documents.associate { doc ->
                    doc.id to (doc.getString("status")?.let {
                        AttendanceStatus.valueOf(it)
                    } ?: AttendanceStatus.PRESENT)
                }

                db.collection("students").get().addOnSuccessListener { studentsSnapshot ->
                    val students = studentsSnapshot.documents.map { doc ->
                        StudentAttendance(
                            studentId = doc.id,
                            fullName = doc.getString("name") ?: "Unknown",
                            status = submittedRecords[doc.id] ?: AttendanceStatus.PRESENT,
                            isSubmitted = true
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
                    attendanceAdapter.submitList(students)
                }
            }.addOnFailureListener { e ->
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
    private val onStatusChanged: (String, AttendanceStatus) -> Unit,
    private val onPermissionAction: (String, Boolean) -> Unit
) : ListAdapter<StudentAttendance, AttendanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewStudentName: TextView = view.findViewById(R.id.textViewStudentName)
        private val chipGroupStatus: ChipGroup = view.findViewById(R.id.chipGroupStatus)
        private val chipPresent: Chip = view.findViewById(R.id.chipPresent)
        private val chipAbsent: Chip = view.findViewById(R.id.chipAbsent)
        private val chipPermissionRequest: Chip = view.findViewById(R.id.chipPermissionRequest)
        private val containerNormalStatus: View = view.findViewById(R.id.containerNormalStatus)


        fun bind(item: StudentAttendance) {
            textViewStudentName.text = item.fullName

            when {
                item.hasPermissionRequest -> {
                    containerNormalStatus.visibility = View.GONE
                    chipPermissionRequest.visibility = View.VISIBLE
                    chipPermissionRequest.setOnClickListener {
                        showPermissionRequestDialog(item)
                    }
                }

                else -> {
                    containerNormalStatus.visibility = View.VISIBLE
                    chipPermissionRequest.visibility = View.GONE
                    setupNormalAttendance(item)
                }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupNormalAttendance(item: StudentAttendance) {
            chipGroupStatus.setOnCheckedChangeListener(null)

            chipPresent.apply {
                text = "Present"
                visibility = View.VISIBLE
            }
            chipAbsent.apply {
                text = "Absent"
                visibility = View.VISIBLE
            }

            when {
                item.isSubmitted -> {
                    when (item.status) {
                        AttendanceStatus.PRESENT -> {
                            chipAbsent.visibility = View.GONE
                            chipPresent.apply {
                                isChecked = true
                                text = "Present"
                            }
                        }

                        AttendanceStatus.ABSENT -> {
                            chipPresent.visibility = View.GONE
                            chipAbsent.apply {
                                isChecked = true
                                visibility = View.VISIBLE
                            }
                        }

                        AttendanceStatus.PERMISSION -> {
                            chipAbsent.visibility = View.GONE
                            chipPresent.apply {
                                isChecked = true
                                text = "Permission"
                            }
                        }
                    }
                }

                else -> {
                    when (item.status) {
                        AttendanceStatus.PRESENT -> {
                            chipPresent.isChecked = true
                            chipAbsent.isChecked = false
                            chipPresent.visibility = View.VISIBLE
                            chipAbsent.visibility = View.VISIBLE
                        }

                        AttendanceStatus.ABSENT -> {
                            chipPresent.isChecked = false
                            chipAbsent.isChecked = true
                            chipPresent.visibility = View.VISIBLE
                            chipAbsent.visibility = View.VISIBLE
                        }

                        AttendanceStatus.PERMISSION -> {
                            chipAbsent.visibility = View.GONE
                            chipPresent.apply {
                                isChecked = true
                                text = "Permission"
                            }
                        }
                    }
                }
            }

            setChipsEnabled(!item.isSubmitted)

            if (!item.isSubmitted) {
                chipGroupStatus.setOnCheckedChangeListener { _, checkedId ->
                    val status = when (checkedId) {
                        R.id.chipPresent -> AttendanceStatus.PRESENT
                        R.id.chipAbsent -> AttendanceStatus.ABSENT
                        else -> AttendanceStatus.PRESENT
                    }
                    onStatusChanged(item.studentId, status)
                }
            }
        }

        private fun setChipsEnabled(enabled: Boolean) {
            chipPresent.isEnabled = enabled
            chipAbsent.isEnabled = enabled

            chipPresent.alpha = if (enabled) 1.0f else 0.6f
            chipAbsent.alpha = if (enabled) 1.0f else 0.6f
        }

        private fun showPermissionRequestDialog(item: StudentAttendance) {
            MaterialAlertDialogBuilder(itemView.context).setView(R.layout.dialog_permission_request)
                .show().apply {
                    findViewById<TextView>(R.id.textViewStudentName)?.text = item.fullName
                    findViewById<TextView>(R.id.textViewRequestDate)?.text = "Date: ${item.date}"
                    findViewById<TextView>(R.id.textViewReason)?.text = item.permissionReason

                    findViewById<MaterialButton>(R.id.buttonApprove)?.setOnClickListener {
                        onPermissionAction(item.studentId, true)
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.buttonReject)?.setOnClickListener {
                        onPermissionAction(item.studentId, false)
                        dismiss()
                    }
                }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StudentAttendance>() {
        override fun areItemsTheSame(
            oldItem: StudentAttendance, newItem: StudentAttendance
        ): Boolean = oldItem.studentId == newItem.studentId

        override fun areContentsTheSame(
            oldItem: StudentAttendance, newItem: StudentAttendance
        ): Boolean = oldItem == newItem
    }
}

data class TeacherAttendanceState(
    val students: List<StudentAttendance> = emptyList(),
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val submissionSuccess: Boolean = false,
    val attendanceSubmitted: Boolean = false,

    )

data class StudentAttendance(
    val studentId: String,
    val fullName: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val isSubmitted: Boolean = false,
    val hasPermissionRequest: Boolean = false,
    val permissionReason: String? = null,
    val date: String? = null
)

sealed class TeacherAttendanceEvent {
    data object LoadStudents : TeacherAttendanceEvent()
    data class UpdateStatus(val studentId: String, val status: AttendanceStatus) :
        TeacherAttendanceEvent()

    data object SubmitAttendance : TeacherAttendanceEvent()
    data class SetDate(val date: String) : TeacherAttendanceEvent()
    data object ClearError : TeacherAttendanceEvent()
    data object ClearSuccess : TeacherAttendanceEvent()
    data class ApprovePermission(val studentId: String) : TeacherAttendanceEvent()
    data class RejectPermission(val studentId: String) : TeacherAttendanceEvent()
}

enum class AttendanceStatus {
    PRESENT, ABSENT, PERMISSION
}