package com.example.studentmanagement.presentation.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.databinding.ItemHomeworkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.text.DateFormat
import java.util.Date

/**
 * @Author: John Youlong.
 * @Date: 6/14/25.
 * @Email: johnyoulong@gmail.com.
 */


class HomeworkViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : BaseViewModel<HomeworkAction, HomeworkUiState>() {

    private val _adapter = HomeworkListAdapter(
        isTeacher = auth.currentUser?.uid != null,
        onUploadSubmission = { homework, uri ->
            onAction(HomeworkAction.UploadSubmission(homework.id, uri))
        },
        onDownloadFile = { homework ->
            onAction(HomeworkAction.DownloadFile(homework.fileUrl, homework.fileName))
        }
    )
    val adapter: HomeworkListAdapter = _adapter

    override fun setInitialState(): HomeworkUiState = HomeworkUiState()

    override fun onAction(event: HomeworkAction) {
        when (event) {
            is HomeworkAction.LoadHomework -> loadHomework()
            is HomeworkAction.UploadHomework -> uploadHomework(event.title, event.fileUri)
            is HomeworkAction.UploadSubmission -> uploadSubmission(event.homeworkId, event.fileUri)
            is HomeworkAction.DownloadFile -> downloadFile(event.fileUrl, event.fileName)
        }
    }

    fun loadHomework() {
        setState { copy(isLoading = true, error = null) }

        firestore.collection("homework")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Failed to load homework: ${e.message}"
                        )
                    }
                    return@addSnapshotListener
                }

                val homework = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Homework::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                setState {
                    copy(
                        isLoading = false,
                        homeworkList = homework,
                        error = null
                    )
                }
                _adapter.submitList(homework)
            }
    }

    private fun uploadHomework(title: String, fileUri: Uri) {
        setState { copy(isLoading = true, error = null) }

        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val storageRef = storage.reference.child("homework/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val homework = Homework(
                        title = title,
                        fileUrl = downloadUrl.toString(),
                        fileName = fileName,
                        uploadedAt = System.currentTimeMillis()
                    )

                    firestore.collection("homework")
                        .add(homework)
                        .addOnSuccessListener {
                            setState { copy(isLoading = false) }
                        }
                        .addOnFailureListener { e ->
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Failed to save homework: ${e.message}"
                                )
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to upload file: ${e.message}"
                    )
                }
            }
    }

    private fun uploadSubmission(homeworkId: String, fileUri: Uri) {
        setState { copy(isLoading = true, error = null) }

        val userId = auth.currentUser?.uid ?: run {
            setState { copy(isLoading = false, error = "User not logged in") }
            return
        }

        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val storageRef = storage.reference.child("submissions/$homeworkId/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    firestore.collection("homework").document(homeworkId)
                        .collection("submissions").document(userId)
                        .set(
                            mapOf(
                                "fileUrl" to downloadUrl.toString(),
                                "fileName" to fileName,
                                "submittedAt" to System.currentTimeMillis()
                            )
                        )
                        .addOnSuccessListener {
                            setState { copy(isLoading = false) }
                        }
                        .addOnFailureListener { e ->
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Failed to save submission: ${e.message}"
                                )
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to upload submission: ${e.message}"
                    )
                }
            }
    }

    private fun downloadFile(fileUrl: String, fileName: String) {
        // Implement download using Android DownloadManager
        // This should be handled in the Fragment/Activity
    }
}

class HomeworkListAdapter(
    private val isTeacher: Boolean,
    private val onUploadSubmission: (Homework, Uri) -> Unit,
    private val onDownloadFile: (Homework) -> Unit
) : ListAdapter<Homework, HomeworkListAdapter.HomeworkViewHolder>(HomeworkDiffCallback()) {

    private var pendingHomework: Homework? = null

    fun submitFile(uri: Uri) {
        pendingHomework?.let { homework ->
            onUploadSubmission(homework, uri)
            pendingHomework = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HomeworkViewHolder(
        private val binding: ItemHomeworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(homework: Homework) {
            binding.apply {
                textTitle.text = homework.title
                textFileName.text = homework.fileName
                textUploadDate.text = formatDate(homework.uploadedAt)

                buttonSubmit.isVisible = !isTeacher
                textSubmissionStatus.isVisible = !isTeacher

                buttonDownload.setOnClickListener { onDownloadFile(homework) }

                if (!isTeacher) {
                    buttonSubmit.setOnClickListener {
                        pendingHomework = homework
                    }

                    textSubmissionStatus.apply {
                        isVisible = homework.submissionUrl != null
                        text = homework.submissionFileName ?: ""
                    }
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            return DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(timestamp))
        }
    }

    private class HomeworkDiffCallback : DiffUtil.ItemCallback<Homework>() {
        override fun areItemsTheSame(oldItem: Homework, newItem: Homework) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Homework, newItem: Homework) =
            oldItem == newItem
    }
}

data class HomeworkUiState(
    val isLoading: Boolean = false,
    val homeworkList: List<Homework>? = null,
    val error: String? = null
)

sealed class HomeworkAction {
    data class UploadHomework(val title: String, val fileUri: Uri) : HomeworkAction()
    data class UploadSubmission(val homeworkId: String, val fileUri: Uri) : HomeworkAction()
    data object LoadHomework : HomeworkAction()
    data class DownloadFile(val fileUrl: String, val fileName: String) : HomeworkAction()
}

data class Homework(
    val id: String = "",
    val title: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val submissionUrl: String? = null,
    val submissionFileName: String? = null,
    val uploadedAt: Long = System.currentTimeMillis()
)



