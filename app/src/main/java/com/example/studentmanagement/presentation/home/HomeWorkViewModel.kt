package com.example.studentmanagement.presentation.home

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.Homework
import com.example.studentmanagement.databinding.ItemHomeworkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
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
        isTeacher = false,
        onUploadSubmission = { homework, uri ->
            onAction(HomeworkAction.UploadSubmission(homework.id, uri))
        },
        onDownloadFile = { homework ->
            onAction(HomeworkAction.DownloadFile(homework.fileUrl, homework.fileName))
        }
    )
    val adapter: HomeworkListAdapter = _adapter


    init {

        checkTeacherStatus()
    }

    private fun checkTeacherStatus() {
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val isTeacher = document.getString("accountType") == "teacher"
                    _adapter.updateTeacherStatus(isTeacher)
                }
                .addOnFailureListener { e ->
                    setState { copy(error = "Failed to verify user role: ${e.message}") }
                }
        }
    }

    override fun setInitialState(): HomeworkUiState = HomeworkUiState()

    override fun onAction(event: HomeworkAction) {
        when (event) {
            is HomeworkAction.LoadHomework -> loadHomework()
            is HomeworkAction.UploadHomework -> uploadHomework(event.title, event.fileUri)
            is HomeworkAction.UploadSubmission -> uploadSubmission(event.homeworkId, event.fileUri)
            is HomeworkAction.DownloadFile -> {
                setState {
                    copy(
                        downloadInfo = DownloadInfo(
                            fileUrl = event.fileUrl,
                            fileName = event.fileName
                        )
                    )
                }
            }
        }
    }

    private fun loadHomework() {
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
        val userId = auth.currentUser?.uid ?: run {
            setState { copy(isLoading = false, error = "User not logged in") }
            return
        }

        val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val storageRef = storage.reference
            .child("homework")  // Root folder
            .child(fileName)    // File name only

        Log.d("HomeworkViewModel", "Starting upload to: ${storageRef.path}")

        val metadata = StorageMetadata.Builder()
            .setContentType("application/pdf") // Adjust based on your file type
            .build()

        storageRef.putFile(fileUri, metadata)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                Log.d("HomeworkViewModel", "Upload progress: $progress%")
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d("HomeworkViewModel", "Upload successful")
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        createHomeworkDocument(title, fileName, downloadUrl.toString())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("HomeworkViewModel", "Upload failed", e)
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to upload: ${e.localizedMessage}"
                    )
                }
            }
    }

    private fun createHomeworkDocument(title: String, fileName: String, fileUrl: String) {
        val homework = hashMapOf(
            "title" to title,
            "fileName" to fileName,
            "fileUrl" to fileUrl,
            "teacherId" to auth.currentUser?.uid,
            "uploadedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("homework")
            .add(homework)
            .addOnSuccessListener {
                setState { copy(isLoading = false) }
                loadHomework()
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to create document: ${e.message}"
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

    private fun downloadFile(context: Context, fileUrl: String, fileName: String) {
        try {
            val request = DownloadManager.Request(fileUrl.toUri())
                .setTitle(fileName)
                .setDescription("Downloading file...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

class HomeworkListAdapter(
    private var isTeacher: Boolean,
    private val onUploadSubmission: (Homework, Uri) -> Unit,
    private val onDownloadFile: (Homework) -> Unit
) : ListAdapter<Homework, HomeworkListAdapter.HomeworkViewHolder>(HomeworkDiffCallback()) {

    private var pendingHomework: Homework? = null
    private var submitClickListener: (() -> Unit)? = null

    fun updateTeacherStatus(isTeacher: Boolean) {
        this.isTeacher = isTeacher
        notifyDataSetChanged()
    }


    fun setOnSubmitClickListener(listener: () -> Unit) {
        submitClickListener = listener
    }

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
                        submitClickListener?.invoke()
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
    val error: String? = null,
    val downloadInfo: DownloadInfo? = null
)

sealed class HomeworkAction {
    data class UploadHomework(val title: String, val fileUri: Uri) : HomeworkAction()
    data class UploadSubmission(val homeworkId: String, val fileUri: Uri) : HomeworkAction()
    data object LoadHomework : HomeworkAction()
    data class DownloadFile(val fileUrl: String, val fileName: String) : HomeworkAction()

}

data class DownloadInfo(
    val fileUrl: String,
    val fileName: String
)



