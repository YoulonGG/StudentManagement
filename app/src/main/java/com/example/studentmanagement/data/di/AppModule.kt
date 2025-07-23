package com.example.studentmanagement.data.di

/**
 * @Author: John Youlong.
 * @Date: 5/30/25.
 * @Email: johnyoulong@gmail.com.
 */

import android.app.Application
import android.content.Context
import com.example.studentmanagement.presentation.ask_permission.StudentPermissionViewModel
import com.example.studentmanagement.presentation.attendace_history.StudentAttendanceViewModel
import com.example.studentmanagement.presentation.create_student.CreateStudentViewModel
import com.example.studentmanagement.presentation.login.LoginViewModel
import com.example.studentmanagement.presentation.reset_password.ResetPasswordViewModel
import com.example.studentmanagement.presentation.sign_up.SignUpViewModel
import com.example.studentmanagement.presentation.student.StudentViewModel
import com.example.studentmanagement.presentation.student_detail.StudentDetailViewModel
import com.example.studentmanagement.presentation.student_list.StudentListViewModel
import com.example.studentmanagement.presentation.student_score.StudentScoreViewModel
import com.example.studentmanagement.presentation.subject_detail.SubjectDetailViewModel
import com.example.studentmanagement.presentation.subjects_list.SubjectListViewModel
import com.example.studentmanagement.presentation.teacher.TeacherViewModel
import com.example.studentmanagement.presentation.teacher_attendance.TeacherAttendanceViewModel
import com.example.studentmanagement.presentation.teacher_profile.TeacherProfileViewModel
import com.example.studentmanagement.presentation.teacher_submit_score.SubmitScoreViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {


    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { Application::class.java }
    single {
        androidContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }


    viewModel { LoginViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { StudentViewModel(get(), get()) }
    viewModel { StudentListViewModel(get()) }
    viewModel { StudentDetailViewModel(get(), get()) }
    viewModel { TeacherAttendanceViewModel(get(), get()) }
    viewModel { StudentAttendanceViewModel(get()) }
    viewModel { StudentPermissionViewModel(get(), get()) }
    viewModel { SubjectListViewModel(get(), get(), get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { TeacherViewModel(get(), get(), get()) }
    viewModel { CreateStudentViewModel(get(), get()) }
    viewModel { TeacherProfileViewModel(get(), get(), get()) }
    viewModel { SubmitScoreViewModel(get()) }
    viewModel { StudentScoreViewModel(get()) }
    viewModel { SubjectDetailViewModel(get(), get()) }

}