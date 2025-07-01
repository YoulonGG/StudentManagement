package com.example.studentmanagement.presentation.teacher_profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentTeacherProfileBinding
import com.example.studentmanagement.presentation.teacher_attendance.TeacherAttendanceViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TeacherAttendanceFragment : Fragment(R.layout.fragment_teacher_profile) {
    private lateinit var binding: FragmentTeacherProfileBinding
    private val viewModel: TeacherAttendanceViewModel by viewModel()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTeacherProfileBinding.bind(view)

    }
}