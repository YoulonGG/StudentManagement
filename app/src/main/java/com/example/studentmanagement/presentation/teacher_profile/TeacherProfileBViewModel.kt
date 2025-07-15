package com.example.studentmanagement.presentation.teacher_profile

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.core.utils.FileUtils
import com.example.studentmanagement.data.dto.TeacherResponse
import com.example.studentmanagement.data.local.PreferencesKeys
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


class TeacherProfileViewModel(
    private val firestore: FirebaseFirestore,
    private val application: Application,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel<TeacherProfileAction, TeacherProfileUiState>() {

    override fun setInitialState(): TeacherProfileUiState = TeacherProfileUiState()

    override fun onAction(event: TeacherProfileAction) {
        when (event) {
            is TeacherProfileAction.LoadTeacher -> {
                setState { copy(teacher = event.teacher) }
            }

            is TeacherProfileAction.LoadCurrentTeacher -> loadCurrentTeacherData()
            is TeacherProfileAction.UploadImage -> uploadImageToStorage(event.imageUri)
            is TeacherProfileAction.SaveTeacher -> saveTeacherToFirestore(event.updatedTeacher)
        }
    }

    private fun loadCurrentTeacherData() {
        val cachedTeacher = loadTeacherFromSharedPrefs()
        if (cachedTeacher != null) {
            setState { copy(teacher = cachedTeacher, isLoading = false) }
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        setState { copy(isLoading = true) }

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val teacher = document.toObject(TeacherResponse::class.java)
                if (teacher != null) {
                    saveTeacherToSharedPrefs(teacher)
                }
                setState { copy(isLoading = false, teacher = teacher) }
            }
            .addOnFailureListener {
                setState { copy(isLoading = false, error = it.message) }
            }
    }

    private fun uploadImageToStorage(uri: Uri) {
        setState { copy(isLoading = true) }

        val file = FileUtils.from(application.applicationContext, uri)
        val cloudName = "dc1qetqkl"
        val uploadPreset = "student_management"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                setState { copy(isLoading = false, error = "Upload failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        val imageUrl = JSONObject(body).getString("secure_url")
                        val updated = uiState.value.teacher?.copy(imageUrl = imageUrl)
                        if (updated != null) {
                            saveTeacherToFirestore(updated)
                        }
                    } catch (e: Exception) {
                        setState { copy(isLoading = false, error = "Parse error: ${e.message}") }
                    }
                } else {
                    setState { copy(isLoading = false, error = "Upload failed") }
                }
            }
        })
    }

    private fun saveTeacherToFirestore(teacher: TeacherResponse) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        setState { copy(isLoading = true) }

        firestore.collection("users").document(uid)
            .set(teacher)
            .addOnSuccessListener {
                saveTeacherToSharedPrefs(teacher)
                setState { copy(isLoading = false, teacher = teacher) }
            }
            .addOnFailureListener {
                setState { copy(isLoading = false, error = it.message) }
            }
    }

    private fun saveTeacherToSharedPrefs(teacher: TeacherResponse) {
        with(sharedPreferences.edit()) {
            putString(PreferencesKeys.TEACHER_USERNAME, teacher.username)
            putString(PreferencesKeys.TEACHER_EMAIL, teacher.email)
            putString(PreferencesKeys.TEACHER_PHONE, teacher.phone)
            putString(PreferencesKeys.TEACHER_ADDRESS, teacher.address)
            putString(PreferencesKeys.TEACHER_AGE, teacher.age)
            putString(PreferencesKeys.TEACHER_GENDER, teacher.gender)
            putString(PreferencesKeys.TEACHER_IMAGE_URL, teacher.imageUrl)
            apply()
        }
    }

    private fun loadTeacherFromSharedPrefs(): TeacherResponse? {
        val username = sharedPreferences.getString(PreferencesKeys.TEACHER_USERNAME, null)
        val email = sharedPreferences.getString(PreferencesKeys.TEACHER_EMAIL, null)

        if (username == null || email == null) {
            return null
        }

        return TeacherResponse(
            accountType = "teacher",
            username = username,
            email = email,
            phone = sharedPreferences.getString(PreferencesKeys.TEACHER_PHONE, null),
            address = sharedPreferences.getString(PreferencesKeys.TEACHER_ADDRESS, null),
            age = sharedPreferences.getString(PreferencesKeys.TEACHER_AGE, null),
            gender = sharedPreferences.getString(PreferencesKeys.TEACHER_GENDER, null),
            imageUrl = sharedPreferences.getString(PreferencesKeys.TEACHER_IMAGE_URL, null)
        )
    }
}
