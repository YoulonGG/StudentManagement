package com.example.studentmanagement.presentation.teacher

import android.content.Context
import android.content.Intent
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
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.presentation.activity.MainActivity
import com.example.studentmanagement.presentation.teacher.components.HomeCardItem
import com.example.studentmanagement.presentation.teacher.components.TeacherHomeCardAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class TeacherScreen : Fragment(R.layout.fragment_teacher_screen) {
    private lateinit var recyclerView: RecyclerView
    private val viewModel: TeacherViewModel by viewModel()
    private lateinit var teacherImage: ImageView
    private lateinit var button: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        teacherImage = view.findViewById(R.id.teacherImage)
        button = view.findViewById(R.id.btnLogOut)

        setupRecyclerView()
        observeViewModel()

        button.setOnClickListener {
            handleLogout()
        }
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.teacherData.collect { state ->
                    view?.findViewById<TextView>(R.id.teacherNameTitle)?.text = state.teacherName

                    view?.findViewById<TextView>(R.id.studentCount)?.let { textView ->
                        textView.text = "Total Students: ${state.totalStudents}"
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
    }

    private fun setupRecyclerView() {
        val items = listOf(
            HomeCardItem(
                1,
                "Check Attendance",
                R.drawable.attendance_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_attendance)
            },
            HomeCardItem(
                2,
                "Attendance Record",
                R.drawable.attendance_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_attendance_history)
            },
            HomeCardItem(
                3,
                "Profile",
                R.drawable.attendance_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_teacher_profile)
            },
            HomeCardItem(
                4,
                "Student List",
                R.drawable.attendance_icon
            ) {
                findNavController().navigate(R.id.navigate_teacher_to_student_list)
            },
            HomeCardItem(
                5,
                "Subjects",
                R.drawable.attendance_icon
            ) { findNavController().navigate(R.id.navigate_teacher_to_subject_list) },
            HomeCardItem(
                6,
                "Create Student",
                R.drawable.attendance_icon
            ) { findNavController().navigate(R.id.navigate_teacher_to_create_student) },
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

    private fun handleLogout() {
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove(PreferencesKeys.IS_LOGGED_IN)
            remove(PreferencesKeys.ACCOUNT_TYPE)
            apply()
        }

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }
}