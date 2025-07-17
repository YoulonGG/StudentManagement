package com.example.studentmanagement.presentation.student_score

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.databinding.FragmentStudentScoreScreenBinding
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

        if (studentId.isNotEmpty()) {
            viewModel.onAction(StudentScoreViewAction.LoadStudentScores(studentId))
        } else {
            showError("Invalid student ID")
        }
    }

    @SuppressLint("SetTextI18n")
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

                state.studentInformation?.let { info ->
                    binding.studentNameText.text = info.name
                    binding.studentIdText.text = "ID: ${info.studentId}"
                    binding.studentEmailText.text = "Email: ${info.email}"
                }

                scoreAdapter.submitList(state.studentScores)
                updateSummaryStats(state.studentScores)

                binding.emptyStateText.isVisible = state.studentScores.isEmpty() && !state.isLoading
                binding.scoresContainer.isVisible = state.studentScores.isNotEmpty()

                if (!state.isLoading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun setupToolbar() {
        val goBack = view?.findViewById<View>(R.id.goBack)
        val toolbarTitle = view?.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle?.text = getString(R.string.my_scores)
        goBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        binding.scoresRecyclerView.apply {
            adapter = scoreAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator?.changeDuration = 300
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setOnRefreshListener {
                if (studentId.isNotEmpty()) {
                    viewModel.onAction(StudentScoreViewAction.RefreshScores(studentId))
                } else {
                    isRefreshing = false
                    showError("Invalid student ID")
                }
            }

            setColorSchemeResources(
                R.color.primary,
                R.color.primary,
                R.color.primary
            )
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

    @SuppressLint("DefaultLocale")
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
        Dialog.showDialog(
            requireContext(),
            title = "Error",
            description = error,
            onBtnClick = { }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}