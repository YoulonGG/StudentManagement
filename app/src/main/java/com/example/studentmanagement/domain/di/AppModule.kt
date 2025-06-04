package com.example.studentmanagement.domain.di

/**
 * @Author: John Youlong.
 * @Date: 5/30/25.
 * @Email: johnyoulong@gmail.com.
 */

import com.example.studentmanagement.presentation.approve_student.ApprovalStudentViewModel
import com.example.studentmanagement.presentation.login.LoginViewModel
import com.example.studentmanagement.presentation.sign_up.SignUpViewModel
import com.example.studentmanagement.presentation.student.StudentViewModel
import com.example.studentmanagement.presentation.student_detail.StudentDetailViewModel
import com.example.studentmanagement.presentation.student_list.StudentListViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }

    viewModel { LoginViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { ApprovalStudentViewModel(get()) }
    viewModel { StudentViewModel(get(), get()) }
    viewModel { StudentListViewModel(get()) }
    viewModel { StudentDetailViewModel(get(), get()) }

}