package com.example.studentmanagement.presentation.student_score

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentStudentScoreScreenBinding
import com.example.studentmanagement.presentation.student_score_view.StudentScoreDetailAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentScoreViewFragment : Fragment(R.layout.fragment_student_score_screen) {

    private var _binding: FragmentStudentScoreScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentScoreViewModel by viewModel()
    private val studentId: String by lazy {
        arguments?.getString("studentId") ?: ""
    }
    private val scoreAdapter = StudentScoreDetailAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentScoreScreenBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        setupSwipeRefresh()
        setupScrollSynchronization()
        observeViewModel()

        viewModel.onAction(StudentScoreViewAction.LoadStudentScores(studentId))
    }

    private fun setupToolbar() {
        val goBack = view?.findViewById<View>(R.id.goBack)
        val toolbarTitle = view?.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle?.text = "My Scores"
        goBack?.setOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        binding.scoresRecyclerView.apply {
            adapter = scoreAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.onAction(StudentScoreViewAction.RefreshScores(studentId))
        }
    }

    private fun setupScrollSynchronization() {
        binding.contentScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            binding.headerScrollView.scrollTo(scrollX, 0)
        }

        binding.headerScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            binding.contentScrollView.scrollTo(scrollX, 0)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible =
                    state.isLoading && !binding.swipeRefresh.isRefreshing
                binding.swipeRefresh.isRefreshing =
                    state.isLoading && binding.swipeRefresh.isRefreshing

                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    binding.swipeRefresh.isRefreshing = false
                }

                if (!state.isLoading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        viewModel.studentInfo.observe(viewLifecycleOwner) { studentInfo ->
            binding.studentNameText.text = studentInfo.name
            binding.studentIdText.text = "ID: ${studentInfo.studentId}"
            binding.studentEmailText.text = studentInfo.email
        }

        viewModel.studentScores.observe(viewLifecycleOwner) { scores ->
            scoreAdapter.submitList(scores)
            updateSummaryStats(scores)

            binding.emptyStateText.isVisible = scores.isEmpty()
            binding.scoresContainer.isVisible = scores.isNotEmpty()
        }
    }

    private fun updateSummaryStats(scores: List<StudentScoreDetail>) {
        if (scores.isEmpty()) {
            binding.totalSubjectsText.text = "0"
            binding.overallGpaText.text = "0.0"
            binding.averageScoreText.text = "0%"
            return
        }

        val totalSubjects = scores.size
        val overallGpa = viewModel.calculateOverallGPA(scores)
        val averagePercentage = scores.map { it.percentage }.average()

        binding.totalSubjectsText.text = totalSubjects.toString()
        binding.overallGpaText.text = String.format("%.1f", overallGpa)
        binding.averageScoreText.text = String.format("%.1f%%", averagePercentage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}