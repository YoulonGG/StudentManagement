package com.example.studentmanagement.presentation.student

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.data.local.PreferencesKeys.ACCOUNT_TYPE
import com.example.studentmanagement.presentation.activity.MainActivity
import com.example.studentmanagement.presentation.teacher.components.HomeCardItem
import com.example.studentmanagement.presentation.teacher.components.TeacherHomeCardAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class StudentScreen : Fragment(R.layout.fragment_student_screen) {

    private val viewModel: StudentViewModel by viewModel()
    private lateinit var studentImage: ImageView
    private lateinit var studentName: TextView
    private lateinit var recyclerView: RecyclerView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onAction(StudentAction.LoadStudentData)
        recyclerView = view.findViewById(R.id.studentRecyclerView)
        studentImage = view.findViewById(R.id.studentImage)
        studentName = view.findViewById(R.id.studentNameTitle)

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) {
                }

                state.error?.let { error ->
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                }

                state.student?.name?.let { name ->
                    studentName.text = name
                } ?: run {
                    studentName.text = "No name available"
                }

                state.student?.imageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_place_holder_profile)
                        .error(R.drawable.ic_place_holder_profile)
                        .into(studentImage)
                }
            }
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val items = listOf(
            HomeCardItem(
                1,
                "Ask Permission",
                R.drawable.attendance_icon
            ) {
                findNavController().navigate(R.id.navigate_student_to_ask_permission)
            },
            HomeCardItem(
                2,
                "Profile",
                R.drawable.attendance_icon
            ) {
                val currentStudent = viewModel.uiState.value.student
                if (currentStudent != null) {
                    val bundle = Bundle().apply {
                        putParcelable("student", currentStudent)
                    }
                    findNavController().navigate(R.id.navigate_student_to_student_profile, bundle)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load student data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            HomeCardItem(
                2,
                "Course",
                R.drawable.attendance_icon
            ) {
//                findNavController().navigate(R.id.navigate_student_list_to_student_details)
            },
            HomeCardItem(
                2,
                "My Subjects",
                R.drawable.attendance_icon
            ) {
//                findNavController().navigate(R.id.navigate_student_list_to_student_details)
            },
            HomeCardItem(
                2,
                "Log Out",
                R.drawable.attendance_icon
            ) {
                val sharedPref =
                    requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove(PreferencesKeys.IS_LOGGED_IN)
                    remove(ACCOUNT_TYPE)
                    apply()
                }

                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
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
