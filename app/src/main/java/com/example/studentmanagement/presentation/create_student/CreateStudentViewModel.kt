package com.example.studentmanagement.presentation.create_student

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentmanagement.data.dto.request.CreateStudentDataRequest
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 5/28/25.
 * @Email: johnyoulong@gmail.com.
 */


class CreateStudentViewModel : ViewModel() {
    private val _state = MutableLiveData<CreateStudentState>()
    val state: LiveData<CreateStudentState> get() = _state

    fun handleIntent(event: CreateStudentAction) {
        when (event) {
            is CreateStudentAction.OnCreateStudent -> {
                submitStudentToFirestore(event.data)
            }
        }
    }

    private fun submitStudentToFirestore(data: CreateStudentDataRequest) {
        _state.value = CreateStudentState(isLoading = true)

        val db = FirebaseFirestore.getInstance()
        val student = hashMapOf(
            "fullName" to data.fullName,
            "email" to data.email,
            "address" to data.address,
            "phone" to data.phone,
            "age" to data.age,
            "studentId" to data.studentId,
            "guardian" to data.guardian,
            "guardianContact" to data.guardianContact,
            "majoring" to data.majoring
        )

        db.collection("students").document(data.studentId ?: "")
            .set(student)
            .addOnSuccessListener {
                _state.value = CreateStudentState(isSuccess = true)
            }
            .addOnFailureListener { e ->
                _state.value = CreateStudentState(errorMessage = e.message)
            }
    }
}
