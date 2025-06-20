package com.example.studentmanagement.presentation.approve_student

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.databinding.ItemStudentApprovalBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 6/2/25.
 * @Email: johnyoulong@gmail.com.
 */

class ApprovalStudentViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<ApprovalStudentAction, ApprovalUiState>() {

    val studentAdapter = StudentApprovalAdapter(
        onApprove = { student -> onAction(ApprovalStudentAction.ApproveStudent(student)) },
        onReject = { student -> onAction(ApprovalStudentAction.RejectStudent(student)) }
    )

    override fun setInitialState(): ApprovalUiState = ApprovalUiState()

    override fun onAction(event: ApprovalStudentAction) {
        when (event) {
            is ApprovalStudentAction.LoadStudents -> loadPendingStudents()
            is ApprovalStudentAction.ApproveStudent -> approveStudent(event.student)
            is ApprovalStudentAction.RejectStudent -> rejectStudent(event.student)
        }
    }

    fun loadPendingStudents() {
        setState { copy(isLoading = true, error = null) }

        firestore.collection("students")
            .whereEqualTo("isApproved", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val students: List<Map<String, Any>> = querySnapshot.documents.map { doc ->
                    mapOf<String, Any>(
                        "id" to doc.id,
                        "name" to (doc.getString("name") ?: ""),
                        "email" to (doc.getString("email") ?: ""),
                        "studentID" to (doc.getString("studentID") ?: ""),
                        "isApproved" to (doc.getBoolean("isApproved") ?: false)
                    )
                }
                studentAdapter.submitList(students)
                setState {
                    copy(
                        isLoading = false,
                        students = students,
                        error = null
                    )
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load students: ${e.message}"
                    )
                }
            }
    }

    private fun approveStudent(student: Map<String, Any>) {
        val studentId = student["id"] as? String ?: return
        setState { copy(isLoading = true) }

        firestore.collection("students").document(studentId)
            .update("isApproved", true)
            .addOnSuccessListener {
                val currentStudents = uiState.value.students
                val updatedList = currentStudents?.filter { it["id"] != studentId }
                studentAdapter.submitList(updatedList)
                setState { copy(isLoading = false, students = updatedList) }
            }
            .addOnFailureListener { e ->
                setState { copy(isLoading = false, error = "Approval failed: ${e.message}") }
            }
    }

    private fun rejectStudent(student: Map<String, Any>) {
        val studentId = student["id"] as? String ?: return
        setState { copy(isLoading = true) }

        firestore.collection("students").document(studentId)
            .delete()
            .addOnSuccessListener {
                val updatedList = uiState.value.students?.filter { it["id"] != studentId }
                studentAdapter.submitList(updatedList)
                setState { copy(isLoading = false, students = updatedList) }
            }
            .addOnFailureListener { e ->
                setState { copy(isLoading = false, error = "Rejection failed: ${e.message}") }
            }
    }


    inner class StudentApprovalAdapter(
        private val onApprove: (Map<String, Any>) -> Unit,
        private val onReject: (Map<String, Any>) -> Unit
    ) : ListAdapter<Map<String, Any>, StudentApprovalAdapter.ViewHolder>(StudentDiffCallback()) {

        inner class ViewHolder(val binding: ItemStudentApprovalBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentApprovalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = getItem(position)
            holder.binding.apply {
                nameText.text = student["name"].toString()
                emailText.text = student["email"].toString()
                phoneText.text = student["studentID"].toString()

                approveButton.setOnClickListener { onApprove(student) }
                rejectButton.setOnClickListener { onReject(student) }
            }
        }

        override fun submitList(list: List<Map<String, Any>>?) {
            super.submitList(list?.let { ArrayList(it) })
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>) =
            oldItem["id"] == newItem["id"]

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>) =
            oldItem == newItem
    }
}

sealed class ApprovalStudentAction {
    data object LoadStudents : ApprovalStudentAction()
    data class ApproveStudent(val student: Map<String, Any>) : ApprovalStudentAction()
    data class RejectStudent(val student: Map<String, Any>) : ApprovalStudentAction()
}

data class ApprovalUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val students: List<Map<String, Any>>? = null
)
