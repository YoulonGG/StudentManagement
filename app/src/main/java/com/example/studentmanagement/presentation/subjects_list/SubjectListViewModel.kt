package com.example.studentmanagement.presentation.subjects_list

import android.app.Application
import android.net.Uri
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
                name = event.name,
                description = event.description,
                code = event.code,
                className = event.className,
                classTime = event.classTime,
                imageUri = event.imageUri
            )
            is SubjectListEvent.DeleteSubject -> deleteSubject(event.subjectId)
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
                        code = doc.getString("code"),
                        className = doc.getString("className"),
                        classTime = doc.getString("classTime"),
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

    private fun createSubject(
        name: String,
        description: String,
        code: String,
        className: String,
        classTime: String,
        imageUri: Uri?
    ) {
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
            uploadImageToCloudinary(name, description, code, className, classTime, imageUri, currentUser.uid)
        } else {
            createSubjectInFirestore(name, description, code, className, classTime, null, currentUser.uid)
        }
    }

    private fun deleteSubject(subjectId: String) {
        setState { copy(isLoading = true) }

        db.collection("subjects")
            .document(subjectId)
            .delete()
            .addOnSuccessListener {
                setState {
                    copy(
                        isLoading = false,
                        successMessage = "Subject deleted successfully!"
                    )
                }
                loadSubjects()
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to delete subject: ${e.message}"
                    )
                }
            }
    }

    private fun uploadImageToCloudinary(
        name: String,
        description: String,
        code: String,
        className: String,
        classTime: String,
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
                            createSubjectInFirestore(name, description, code, className, classTime, imageUrl, teacherId)
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
        code: String,
        className: String,
        classTime: String,
        imageUrl: String?,
        teacherId: String
    ) {
        val subjectData = hashMapOf(
            "name" to name,
            "description" to description,
            "code" to code,
            "className" to className,
            "classTime" to classTime,
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
        val code: String,
        val className: String,
        val classTime: String,
        val imageUri: Uri?
    ) : SubjectListEvent()
    data class DeleteSubject(val subjectId: String) : SubjectListEvent()
}