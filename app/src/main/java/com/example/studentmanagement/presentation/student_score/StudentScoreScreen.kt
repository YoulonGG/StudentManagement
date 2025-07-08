package com.example.studentmanagement.presentation.student_score

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentStudentScoreScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentScoreFragment : Fragment(R.layout.fragment_student_score_screen) {

    private var _binding: FragmentStudentScoreScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StudentScoreViewModel by viewModel()
    private val scoreAdapter = StudentScoreViewAdapter() // Changed to StudentScoreViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStudentScoreScreenBinding.bind(view)

        setupRecyclerView()
        setupScrollSynchronization()
        observeViewModel()
        fetchScores()
    }

    private fun setupRecyclerView() {
        binding.studentScoreRecycler.apply {
            adapter = scoreAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
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

    private fun fetchScores() {
        val studentId = arguments?.getString("studentId") ?: return
        viewModel.fetchScores(studentId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.emptyView.isVisible = !state.isLoading && viewModel.studentScores.value?.isEmpty() == true

                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.studentScores.observe(viewLifecycleOwner) { scores ->
            scoreAdapter.submitList(scores)
            updateAverageScore(scores)
        }
    }

    private fun updateAverageScore(scores: List<ScoreItem>) {
        val average = scores.map { it.total }.average().takeIf { !it.isNaN() } ?: 0.0
        binding.averageScoreText.text = getString(R.string.overall_average).format(average)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(studentId: String): StudentScoreFragment {
            return StudentScoreFragment().apply {
                arguments = Bundle().apply {
                    putString("studentId", studentId)
                }
            }
        }
    }
}