package com.example.studentmanagement.presentation.subjects_list

import android.app.Application
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.core.utils.FileUtils
import com.example.studentmanagement.data.dto.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

/**
 * @Author: John Youlong.
 * @Date: 6/17/25.
 * @Email: johnyoulong@gmail.com.
 */


class SubjectListViewModel(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val application: Application
) : BaseViewModel<SubjectListEvent, SubjectListState>() {

    override fun setInitialState() = SubjectListState()

    override fun onAction(event: SubjectListEvent) {
        when (event) {
            SubjectListEvent.LoadSubjects -> loadSubjects()
            SubjectListEvent.ClearError -> setState { copy(error = null) }
            is SubjectListEvent.CreateSubject -> createSubject(
                event.name,
                event.description,
                event.imageUri
            )
        }
    }

    private fun loadSubjects() {
        setState { copy(isLoading = true) }

        db.collection("subjects")
            .get()
            .addOnSuccessListener { snapshot ->
                val subjects = snapshot.documents.map { doc ->
                    Subject(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description"),
                        imageUrl = doc.getString("imageUrl"),
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

    private fun createSubject(name: String, description: String, imageUri: Uri?) {
        setState { copy(isCreating = true) }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            setState {
                copy(
                    isCreating = false,
                    error = "User not authenticated"
                )
            }
            return
        }

        if (imageUri != null) {
            uploadImageToCloudinary(name, description, imageUri, currentUser.uid)
        } else {
            createSubjectInFirestore(name, description, null, currentUser.uid)
        }
    }

    private fun uploadImageToCloudinary(
        name: String,
        description: String,
        imageUri: Uri,
        teacherId: String
    ) {
        try {
            val file = FileUtils.from(application.applicationContext, imageUri)
            val cloudName = "dc1qetqkl"
            val uploadPreset = "student_management"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("upload_preset", uploadPreset)
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                .post(requestBody)
                .build()

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    setState {
                        copy(
                            isCreating = false,
                            error = "Upload failed: ${e.message}"
                        )
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && body != null) {
                        try {
                            val imageUrl = JSONObject(body).getString("secure_url")
                            createSubjectInFirestore(name, description, imageUrl, teacherId)
                        } catch (e: Exception) {
                            setState {
                                copy(
                                    isCreating = false,
                                    error = "Parse error: ${e.message}"
                                )
                            }
                        }
                    } else {
                        setState {
                            copy(
                                isCreating = false,
                                error = "Upload failed"
                            )
                        }
                    }
                }
            })
        } catch (e: Exception) {
            setState {
                copy(
                    isCreating = false,
                    error = "Failed to prepare image: ${e.message}"
                )
            }
        }
    }

    private fun createSubjectInFirestore(
        name: String,
        description: String,
        imageUrl: String?,
        teacherId: String
    ) {
        val subjectData = hashMapOf(
            "name" to name,
            "description" to description,
            "imageUrl" to imageUrl,
            "teacherId" to teacherId,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("subjects")
            .add(subjectData)
            .addOnSuccessListener { documentRef ->
                setState {
                    copy(
                        isCreating = false,
                        successMessage = "Subject created successfully!"
                    )
                }
                // Reload subjects to show the new one
                loadSubjects()
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isCreating = false,
                        error = "Failed to create subject: ${e.message}"
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
                .circleCrop()
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
    val isCreating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class SubjectListEvent {
    data object LoadSubjects : SubjectListEvent()
    data object ClearError : SubjectListEvent()
    data class CreateSubject(
        val name: String,
        val description: String,
        val imageUri: Uri?
    ) : SubjectListEvent()
}
