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
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentScoreViewFragment : Fragment(R.layout.fragment_student_score_screen) {

    private var _binding: FragmentStudentScoreScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentScoreViewModel by viewModel()
    private val studentId: String by lazy {
        val id = arguments?.getString("studentId") ?: ""
        id
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

        if (studentId.isNotEmpty()) {
            viewModel.onAction(StudentScoreViewAction.LoadStudentScores(studentId))
        } else {
            showError("Invalid student ID")
        }
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
            if (studentId.isNotEmpty()) {
                viewModel.onAction(StudentScoreViewAction.RefreshScores(studentId))
            } else {
                binding.swipeRefresh.isRefreshing = false
                showError("Invalid student ID")
            }
        }
    }

    private var isSyncingScroll = false
    private fun setupScrollSynchronization() {

        binding.contentScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            if (!isSyncingScroll) {
                isSyncingScroll = true
                binding.headerScrollView.scrollTo(scrollX, 0)
                isSyncingScroll = false
            }
        }

        binding.headerScrollView.setOnScrollChangeListener { _, scrollX, _, _, _ ->
            if (!isSyncingScroll) {
                isSyncingScroll = true
                binding.contentScrollView.scrollTo(scrollX, 0)
                isSyncingScroll = false
            }
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
                    showError(error)
                    binding.swipeRefresh.isRefreshing = false
                }

                if (!state.isLoading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        viewModel.studentInfo.observe(viewLifecycleOwner) { studentInfo ->
            if (studentInfo != null) {
                binding.studentNameText.text = studentInfo.name
                binding.studentIdText.text = "ID: ${studentInfo.studentId}"
                binding.studentEmailText.text = studentInfo.email
            }
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

    private fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}