package com.example.studentmanagement.presentation.subjects_list

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */


class SubjectListViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : BaseViewModel<SubjectListEvent, SubjectListState>() {

    override fun setInitialState() = SubjectListState()

    override fun onAction(event: SubjectListEvent) {
        when (event) {
            SubjectListEvent.LoadSubjects -> loadSubjects()
            SubjectListEvent.ClearError -> setState { copy(error = null) }
            is SubjectListEvent.CreateSubject -> {
                createSubject(
                    event.name,
                    event.description,
                    event.imageUri,
                    event.context
                )
            }
        }
    }

    private suspend fun uploadImage(imageUri: Uri, context: Context): String =
        suspendCoroutine { continuation ->
            try {
                val fileName = "subjects/${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(fileName)

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: throw IllegalArgumentException("Cannot open input stream from URI: $imageUri")

                val uploadTask = storageRef.putStream(inputStream)

                uploadTask
                    .addOnSuccessListener {
                        storageRef.downloadUrl
                            .addOnSuccessListener { downloadUri ->
                                Log.d("UploadImage", "Uploaded. Download URL: $downloadUri")
                                continuation.resume(downloadUri.toString())
                            }
                            .addOnFailureListener { e ->
                                Log.e("UploadImage", "Failed to get download URL", e)
                                continuation.resumeWithException(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UploadImage", "Image upload failed", e)
                        continuation.resumeWithException(e)
                    }
            } catch (e: Exception) {
                Log.e("UploadImage", "Exception during upload", e)
                continuation.resumeWithException(e)
            }
        }

    private fun createSubject(
        name: String,
        description: String,
        imageUri: Uri?,
        context: Context
    ) {
        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val imageUrl = imageUri?.let {
                    uploadImage(it, context)
                } ?: ""

                val docRef = db.collection("subjects").document()
                val id = docRef.id

                val subjectWithId = Subject(
                    id = id,
                    name = name,
                    description = description,
                    imageUrl = imageUrl,
                    teacherId = auth.currentUser?.uid ?: "",
                    createdAt = System.currentTimeMillis()
                )
                docRef.set(subjectWithId).await()
                loadSubjects()

                setState { copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                Log.e("SubjectListViewModel", "Failed to create subject", e)
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to create subject: ${e.localizedMessage ?: e.toString()}"
                    )
                }
            }
        }
    }


    private fun loadSubjects() {
        setState { copy(isLoading = true) }

        db.collection("subjects")
            .whereEqualTo("teacherId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val subjects = snapshot.documents.map { doc ->
                    Subject(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description"),
                        imageUrl = doc.getString("imageUrl"),
                        teacherId = doc.getString("teacherId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }
                setState {
                    copy(
                        subjects = subjects,
                        isLoading = false
                    )
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to load subjects: ${e.message}"
                    )
                }
            }
    }
}

class SubjectAdapter(
    private val onSubjectClicked: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewName: TextView = view.findViewById(R.id.textViewSubjectName)
        private val imageViewSubject: ImageView = view.findViewById(R.id.imageViewSubjectIcon)
        private val textViewSubjectDescription: TextView = view.findViewById(R.id.textViewSubjectDes)
        private val root: View = view.findViewById(R.id.root)

        fun bind(subject: Subject) {
            textViewName.text = subject.name
            textViewSubjectDescription.text = subject.description ?: "No description available"
            imageViewSubject.id = View.generateViewId()
            Glide.with(imageViewSubject.context)
                .load(subject.imageUrl)
                .into(imageViewSubject)
            root.setOnClickListener { onSubjectClicked(subject) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean =
            oldItem == newItem
    }
}

data class SubjectListState(
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SubjectListEvent {
    data object LoadSubjects : SubjectListEvent()
    data object ClearError : SubjectListEvent()

    data class CreateSubject(
        val name: String,
        val description: String,
        val imageUri: Uri?,
        val context: Context
    ) : SubjectListEvent()
}
