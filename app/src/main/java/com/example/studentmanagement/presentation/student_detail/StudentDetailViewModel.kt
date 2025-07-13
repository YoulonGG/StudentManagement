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
            }

            is StudentDetailAction.SaveStudent -> saveStudentToFirestore(event.updatedStudent)
            is StudentDetailAction.UploadImage -> {
                uploadImageToStorage(event.imageUri)
            }

            StudentDetailAction.LoadCurrentStudent -> {
                loadCurrentStudentData()
            }

            StudentDetailAction.ClearError -> {
                setState { copy(error = null) }
            }
        }
    }

    private fun uploadImageToStorage(uri: Uri) {
        setState { copy(isLoading = true, error = null) }

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

                        val updatedStudent = uiState.value.student?.copy(imageUrl = imageUrl)

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
            setState { copy(isLoading = false, error = "No student loaded") }
            return
        }

        // Fixed: Properly preserve name, studentID, and other essential fields
        val updatedStudent = current.copy(
            name = student.name ?: current.name,                      // Preserve if null
            studentID = student.studentID ?: current.studentID,      // Preserve if null
            email = student.email ?: current.email,                  // Use new value or preserve
            address = student.address ?: current.address,            // Use new value or preserve
            phone = student.phone ?: current.phone,                  // Use new value or preserve
            age = student.age ?: current.age,                        // Use new value or preserve
            imageUrl = student.imageUrl ?: current.imageUrl,         // Preserve if null
            guardian = student.guardian ?: current.guardian,         // Use new value or preserve
            guardianContact = student.guardianContact ?: current.guardianContact, // Use new value or preserve
            majoring = student.majoring ?: current.majoring,         // Use new value or preserve
            authUid = current.authUid                                // Always preserve authUid
        )

        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            setState { copy(isLoading = false, error = "User not signed in") }
            return
        }

        // Prepare update data, filtering out null values
        val allowedFields = mapOf(
            "name" to updatedStudent.name,
            "email" to updatedStudent.email,
            "address" to updatedStudent.address,
            "phone" to updatedStudent.phone,
            "age" to updatedStudent.age,
            "imageUrl" to updatedStudent.imageUrl,
            "guardian" to updatedStudent.guardian,
            "guardianContact" to updatedStudent.guardianContact,
            "majoring" to updatedStudent.majoring,
            "studentID" to updatedStudent.studentID
        ).filterValues { it != null }

        firestore.collection("students")
            .document(updatedStudent.authUid ?: authUid)
            .update(allowedFields)
            .addOnSuccessListener {
                setState { copy(isLoading = false, student = updatedStudent, error = null) }
            }
            .addOnFailureListener { e ->
                setState { copy(isLoading = false, error = "Failed to save: ${e.message}") }
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
                                // Create initial student profile
                                val initialStudent = StudentResponse(
                                    authUid = currentUser,
                                    name = userDoc.getString("name") ?: "",
                                    email = userDoc.getString("email") ?: "",
                                    studentID = generateStudentID() // Generate a student ID
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
                    setState { copy(isLoading = false, error = "User is not a student") }
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
    }

    private fun generateStudentID(): String {
        // Generate a simple student ID with current timestamp
        val currentTime = System.currentTimeMillis()
        return "STU${currentTime.toString().takeLast(6)}"
    }
}

sealed class StudentDetailAction {
    data class LoadStudent(val student: StudentResponse) : StudentDetailAction()
    data class SaveStudent(val updatedStudent: StudentResponse) : StudentDetailAction()
    data class UploadImage(val imageUri: Uri) : StudentDetailAction()
    data object LoadCurrentStudent : StudentDetailAction()
    data object ClearError : StudentDetailAction()
}

data class StudentDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: StudentResponse? = null
)