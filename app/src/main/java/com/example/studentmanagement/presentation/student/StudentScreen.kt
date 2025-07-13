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
    private lateinit var btnLogOut: TextView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onAction(StudentAction.LoadStudentData)
        recyclerView = view.findViewById(R.id.studentRecyclerView)
        studentImage = view.findViewById(R.id.studentImage)
        studentName = view.findViewById(R.id.studentNameTitle)
        btnLogOut = view.findViewById(R.id.btnStudentLogOut)

        btnLogOut.setOnClickListener {
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
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) {
                    studentName.text = "Loading..."
                }

                state.error?.let { error ->
                    Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
                }

                state.student?.name?.let { name ->
                    studentName.text = name
                }

                view.findViewById<TextView>(R.id.studentCount)?.let { textView ->
                    textView.text = "${state.totalStudents}"
                }

                view.findViewById<TextView>(R.id.maleStudentCount)?.let { textView ->
                    textView.text = "Male: ${state.maleStudents}"
                }

                view.findViewById<TextView>(R.id.femaleStudentCount)?.let { textView ->
                    textView.text = "Female: ${state.femaleStudents}"
                }

                state.student?.imageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_place_holder_profile)
                        .error(R.drawable.ic_place_holder_profile)
                        .into(studentImage)
                } ?: run {
                    studentImage.setImageResource(R.drawable.ic_place_holder_profile)
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
                R.drawable.student_ask_permission_icon
            ) {
                findNavController().navigate(R.id.navigate_student_to_ask_permission)
            },
            HomeCardItem(
                2,
                "Profile",
                R.drawable.teacher_profile_card_icon
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
                "My Subjects",
                R.drawable.attendance_icon
            ) {
//                findNavController().navigate(R.id.navigate_student_list_to_student_details)
            },
            HomeCardItem(
                2,
                "My Scores",
                R.drawable.attendance_icon
            ) {
//                findNavController().navigate(R.id.navigate_student_list_to_student_details)
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
