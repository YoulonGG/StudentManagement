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

        initializeViews(view)
        setupClickListeners()
        setupRecyclerView()
        observeViewModel()

        viewModel.onAction(StudentAction.LoadStudentData)
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.studentRecyclerView)
        studentImage = view.findViewById(R.id.studentImage)
        studentName = view.findViewById(R.id.studentNameTitle)
        btnLogOut = view.findViewById(R.id.btnStudentLogOut)
    }

    private fun setupClickListeners() {
        btnLogOut.setOnClickListener { handleLogout() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUIState(state)
            }
        }
    }

    private fun handleUIState(state: StudentUiState) {
        if (state.isLoading) {
            studentName.text = "Loading..."
        }

        state.error?.let { error ->
            showError(error)
        }

        state.student?.name?.let { name ->
            studentName.text = name
        }

        updateStudentCounts(state)
        updateStudentImage(state.student?.imageUrl)
    }

    private fun updateStudentCounts(state: StudentUiState) {
        view?.findViewById<TextView>(R.id.studentCount)?.text = "${state.totalStudents}"
        view?.findViewById<TextView>(R.id.maleStudentCount)?.text = "Male : ${state.maleStudents}"
        view?.findViewById<TextView>(R.id.femaleStudentCount)?.text =
            "Female : ${state.femaleStudents}"
    }

    private fun updateStudentImage(imageUrl: String?) {
        imageUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_place_holder_profile)
                .error(R.drawable.ic_place_holder_profile)
                .into(studentImage)
        } ?: run {
            studentImage.setImageResource(R.drawable.ic_place_holder_profile)
        }
    }

    private fun handleLogout() {
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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

    private fun showError(error: String) {
        Toast.makeText(
            requireContext(),
            "getString(R.string.error_message, error)",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setupRecyclerView() {
        val items = createHomeCardItems()
        val adapter = TeacherHomeCardAdapter(items)
        recyclerView.adapter = adapter

        addItemDecoration()
    }

    private fun createHomeCardItems(): List<HomeCardItem> {
        return listOf(
            HomeCardItem(
                1,
                getString(R.string.ask_permission),
                R.drawable.student_ask_permission_icon
            ) {
                findNavController().navigate(R.id.navigate_student_to_ask_permission)
            },
            HomeCardItem(
                2,
                getString(R.string.profile),
                R.drawable.teacher_profile_card_icon
            ) {
                navigateToProfile()
            },
            HomeCardItem(
                3,
                getString(R.string.my_subjects),
                R.drawable.attendance_icon
            ) {

            },
            HomeCardItem(
                4,
                getString(R.string.my_scores),
                R.drawable.attendance_icon
            ) {
                navigateToScores()
            }
        )
    }

    private fun navigateToProfile() {
        val currentStudent = viewModel.uiState.value.student
        if (currentStudent != null) {
            val bundle = Bundle().apply {
                putParcelable("student", currentStudent)
            }
            findNavController().navigate(R.id.navigate_student_to_student_profile, bundle)
        } else {
            showError(getString(R.string.failed_to_load_student_data))
        }
    }

    private fun navigateToScores() {
        val studentId = getCurrentStudentId()
        if (studentId.isNotEmpty()) {
            val bundle = Bundle().apply {
                putString("studentId", studentId)
            }
            findNavController().navigate(R.id.navigate_student_to_student_score, bundle)
        } else {
            showError(getString(R.string.student_id_not_found))
        }
    }

    private fun getCurrentStudentId(): String {
        return viewModel.uiState.value.student?.studentID ?: run {
            val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            sharedPref.getString("current_student_id", "") ?: ""
        }
    }

    private fun addItemDecoration() {
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