package com.example.studentmanagement.presentation.teacher

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.presentation.teacher.components.TeacherHomeCardAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class TeacherScreen : Fragment(R.layout.fragment_teacher_screen) {
    private lateinit var recyclerView: RecyclerView
    private val viewModel: TeacherViewModel by viewModel()
    private lateinit var teacherImage: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        teacherImage = view.findViewById(R.id.teacherImage)

        setupRecyclerView()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    view?.findViewById<TextView>(R.id.teacherNameTitle)?.text = state.teacherName

                    view?.findViewById<TextView>(R.id.studentCount)?.let { textView ->
                        textView.text = "${state.totalStudents}"
                    }

                    view?.findViewById<TextView>(R.id.maleStudentCount)?.let { textView ->
                        textView.text = "Male: ${state.maleStudents}"
                    }

                    view?.findViewById<TextView>(R.id.femaleStudentCount)?.let { textView ->
                        textView.text = "Female: ${state.femaleStudents}"
                    }

                    state.profileImageUrl?.let { imageUrl ->
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_place_holder_profile)
                            .error(R.drawable.ic_place_holder_profile)
                            .circleCrop()
                            .into(teacherImage)
                    } ?: run {
                        teacherImage.setImageResource(R.drawable.ic_place_holder_profile)
                    }

                    if (state.isLoading) {
                        view?.findViewById<TextView>(R.id.teacherNameTitle)?.text = "Loading..."
                    }

                    state.error?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        viewModel.onAction(TeacherAction.LoadTeacherData)
        viewModel.onAction(TeacherAction.LoadStudentCounts)
    }

    private fun setupRecyclerView() {
        val items = listOf(
            HomeCardItem(
                1,
                "Profile",
                R.drawable.teacher_profile_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_teacher_profile)
            },
            HomeCardItem(
                2,
                "Submit Score",
                R.drawable.teacher_submit_score_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_submit_score)
            },

            HomeCardItem(
                3,
                "Attendance Record",
                R.drawable.teacher_attendance_record_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_attendance_history)
            },
            HomeCardItem(
                4,
                "Check Attendance",
                R.drawable.teacher_check_attendance_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_attendance)
            },

            HomeCardItem(
                4,
                "Student List",
                R.drawable.teacher_student_list_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_student_list)
            },
            HomeCardItem(
                5,
                "Create Student",
                R.drawable.crate_student_icon
            ) { findNavController().navigate(R.id.navigate_teacher_to_create_student) },
            HomeCardItem(
                6,
                "Subjects",
                R.drawable.teacher_subjects_card_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_subject_list)
            },
        )

        val adapter = TeacherHomeCardAdapter(items)
        recyclerView.adapter = adapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.card_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = spacingInPixels
                outRect.right = spacingInPixels
                outRect.top = spacingInPixels
                outRect.bottom = spacingInPixels
            }
        })
    }
}