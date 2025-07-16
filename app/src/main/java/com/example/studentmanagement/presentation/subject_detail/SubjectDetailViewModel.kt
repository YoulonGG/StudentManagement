package com.example.studentmanagement.presentation.subject_detail

import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

class SubjectDetailViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : BaseViewModel<SubjectDetailEvent, SubjectDetailState>() {

    override fun setInitialState() = SubjectDetailState()

    override fun onAction(event: SubjectDetailEvent) {
        when (event) {
            is SubjectDetailEvent.LoadSubjectDetail -> loadSubjectDetail(event.subjectId)
            SubjectDetailEvent.ClearError -> setState { copy(error = null) }
            SubjectDetailEvent.ClearSuccessMessage -> setState { copy(successMessage = null) }
            is SubjectDetailEvent.UpdateSubject -> updateSubject(event.subject)
        }
    }

    private fun loadSubjectDetail(subjectId: String) {
        setState { copy(isLoading = true) }

        db.collection("subjects")
            .document(subjectId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val subject = Subject(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        description = document.getString("description"),
                        code = document.getString("code"),
                        className = document.getString("className"),
                        classTime = document.getString("classTime"),
                        imageUrl = document.getString("imageUrl"),
                    )

                    val detailItems = createDetailItems(subject)

                    setState {
                        copy(
                            subject = subject,
                            detailItems = detailItems,
                            isLoading = false
                        )
                    }
                } else {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Subject not found"
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load subject details: ${e.message}"
                    )
                }
            }
    }

    private fun createDetailItems(subject: Subject): List<SubjectDetailItem> {
        val items = mutableListOf<SubjectDetailItem>()

        items.add(SubjectDetailItem.SectionHeader("Basic Information"))
        items.add(SubjectDetailItem.DetailRow("Subject Name", subject.name))
        items.add(SubjectDetailItem.DetailRow("Subject Code", subject.code ?: "N/A"))
        items.add(SubjectDetailItem.DetailRow("Description", subject.description ?: "No description"))

        items.add(SubjectDetailItem.SectionHeader("Class Information"))
        items.add(SubjectDetailItem.DetailRow("Class Name", subject.className ?: "N/A"))
        items.add(SubjectDetailItem.DetailRow("Class Time", subject.classTime ?: "N/A"))

        return items
    }

    private fun updateSubject(subject: Subject) {
        setState { copy(isLoading = true) }

        val updateData = hashMapOf(
            "name" to subject.name,
            "description" to subject.description,
            "code" to subject.code,
            "className" to subject.className,
            "classTime" to subject.classTime,
            "imageUrl" to subject.imageUrl,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("subjects")
            .document(subject.id)
            .update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                setState {
                    copy(
                        isLoading = false,
                        successMessage = "Subject updated successfully!"
                    )
                }
                // Reload the subject details
                loadSubjectDetail(subject.id)
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to update subject: ${e.message}"
                    )
                }
            }
    }
}