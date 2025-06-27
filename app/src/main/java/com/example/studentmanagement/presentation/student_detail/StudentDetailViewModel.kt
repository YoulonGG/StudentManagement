package com.example.studentmanagement.presentation.student_detail

import android.app.Application
import android.net.Uri
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.core.utils.FileUtils
import com.example.studentmanagement.data.dto.StudentResponse
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
 * @Date: 6/2/25.
 * @Email: johnyoulong@gmail.com.
 */

class StudentDetailViewModel(
    private val firestore: FirebaseFirestore,
    private val application: Application
) : BaseViewModel<StudentDetailAction, StudentDetailUiState>() {

    override fun setInitialState(): StudentDetailUiState = StudentDetailUiState()

    override fun onAction(event: StudentDetailAction) {
        when (event) {
            is StudentDetailAction.LoadStudent -> {
                setState { copy(student = event.student) }
                checkUserRole()
            }

            is StudentDetailAction.SaveStudent -> saveStudentToFirestore(event.updatedStudent)
            is StudentDetailAction.UploadImage -> {
                if (!uiState.value.isTeacher) {
                    uploadImageToStorage(event.imageUri)
                }
            }

            StudentDetailAction.LoadCurrentStudent -> {
                loadCurrentStudentData()
            }
        }
    }

    private fun uploadImageToStorage(uri: Uri) {
        setState { copy(isLoading = true) }

        val context = application.applicationContext
        val file = FileUtils.from(context, uri)

        val cloudName = "dc1qetqkl"
        val uploadPreset = "student_management"

        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                setState { copy(isLoading = false, error = "Upload failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    setState {
                        copy(
                            isLoading = false,
                            error = "Upload failed with code: ${response.code}"
                        )
                    }
                    return
                }

                val body = response.body?.string()
                if (body != null) {
                    try {
                        val json = JSONObject(body)
                        val imageUrl = json.getString("secure_url")

                        val updatedStudent =
                            uiState.value.student?.copy(imageUrl = imageUrl)

                        if (updatedStudent != null) {
                            saveStudentToFirestore(updatedStudent)
                        } else {
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Failed to update student data"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        setState { copy(isLoading = false, error = "Parse error: ${e.message}") }
                    }
                } else {
                    setState { copy(isLoading = false, error = "Empty response from Cloudinary") }
                }
            }
        })
    }

    private fun saveStudentToFirestore(student: StudentResponse) {
        setState { copy(isLoading = true, error = null) }

        val current = uiState.value.student ?: run {
            setState { copy(error = "No student loaded") }
            return
        }

        val updatedStudent = current.copy(
            name = student.name,
            email = student.email,
            address = student.address,
            phone = student.phone,
            age = student.age,
            imageUrl = student.imageUrl,
            studentID = student.studentID,
            guardian = student.guardian,
            guardianContact = student.guardianContact,
            majoring = student.majoring,
            authUid = current.authUid
        )

        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            setState { copy(isLoading = false, error = "User not signed in") }
            return
        }

        firestore.collection("users").document(authUid).get()
            .addOnSuccessListener { document ->
                val isTeacher = document.getString("accountType") == "teacher"

                val allowedFields = if (isTeacher) {
                    mapOf(
                        "name" to updatedStudent.name,
                        "email" to updatedStudent.email,
                        "address" to updatedStudent.address,
                        "phone" to updatedStudent.phone,
                        "age" to updatedStudent.age,
                        "imageUrl" to updatedStudent.imageUrl,
                        "guardian" to updatedStudent.guardian,
                        "guardianContact" to updatedStudent.guardianContact,
                        "majoring" to updatedStudent.majoring
                    )
                } else {
                    mapOf(
                        "name" to updatedStudent.name,
                        "imageUrl" to updatedStudent.imageUrl
                    )
                }.filterValues { it != null }

                firestore.collection("students")
                    .document(updatedStudent.authUid ?: "")
                    .update(allowedFields)
                    .addOnSuccessListener {
                        setState { copy(isLoading = false, student = updatedStudent) }
                    }
                    .addOnFailureListener { e ->
                        setState { copy(isLoading = false, error = e.message) }
                    }

            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to fetch user role: ${e.message}"
                    )
                }
            }
    }

    private fun checkUserRole() {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(authUid).get()
            .addOnSuccessListener { document ->
                val isTeacher = document.getString("accountType") == "teacher"
                setState { copy(isTeacher = isTeacher) }
            }
    }

    private fun loadCurrentStudentData() {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            setState { copy(error = "User not authenticated") }
            return
        }

        setState { copy(isLoading = true, error = null) }

        firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener { userDoc ->
                val accountType = userDoc.getString("accountType")
                val isTeacher = accountType == "teacher"
                setState { copy(isTeacher = isTeacher) }

                if (accountType == "student") {
                    firestore.collection("students")
                        .document(currentUser)
                        .get()
                        .addOnSuccessListener { studentDoc ->
                            if (studentDoc.exists()) {
                                val student = studentDoc.toObject(StudentResponse::class.java)
                                setState {
                                    copy(
                                        isLoading = false,
                                        student = student,
                                        error = null
                                    )
                                }
                            } else {
                                val initialStudent = StudentResponse(
                                    authUid = currentUser,
                                    name = userDoc.getString("name") ?: "",
                                    email = userDoc.getString("email") ?: ""
                                )

                                firestore.collection("students")
                                    .document(currentUser)
                                    .set(initialStudent)
                                    .addOnSuccessListener {
                                        setState {
                                            copy(
                                                isLoading = false,
                                                student = initialStudent,
                                                error = null
                                            )
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        setState {
                                            copy(
                                                isLoading = false,
                                                error = "Failed to create student profile: ${e.message}"
                                            )
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Failed to load student data: ${e.message}"
                                )
                            }
                        }
                } else {
                    setState { copy(isLoading = false) }
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = "Failed to check user role: ${e.message}"
                    )
                }
            }
    }}


sealed class StudentDetailAction {
    data class LoadStudent(val student: StudentResponse) : StudentDetailAction()
    data class SaveStudent(val updatedStudent: StudentResponse) : StudentDetailAction()
    data class UploadImage(val imageUri: Uri) : StudentDetailAction()
    data object LoadCurrentStudent : StudentDetailAction()

}


data class StudentDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null,
    val isTeacher: Boolean = false
)



