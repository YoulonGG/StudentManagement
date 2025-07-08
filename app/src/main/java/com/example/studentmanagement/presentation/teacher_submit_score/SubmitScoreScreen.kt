package com.example.studentmanagement.presentation.teacher_submit_score

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.databinding.FragmentSubmitScoreScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubmitScoreFragment : Fragment(R.layout.fragment_submit_score_screen) {

    private var _binding: FragmentSubmitScoreScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SubmitScoreViewModel by viewModel()
    private val scoreAdapter = StudentScoreAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSubmitScoreScreenBinding.bind(view)

        setupRecyclerView()
        setupScrollSynchronization()
        setupSpinners()
        setupSubmitButton()
        observeViewModel()
        fetchScores()
    }

    private fun setupRecyclerView() {
        binding.studentScoreRecycler.apply {
            adapter = scoreAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
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

    private fun setupSpinners() {
        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fetchScores()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.semesterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    fetchScores()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupSubmitButton() {
        binding.submitScoresBtn.setOnClickListener {
            val year = binding.yearSpinner.selectedItem.toString().toInt()
            val semester = binding.semesterSpinner.selectedItem.toString().toInt()
            val currentScores = scoreAdapter.currentList

            if (currentScores.isEmpty()) {
                Toast.makeText(requireContext(), "No scores to submit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentScores.any { score ->
                    score.assignment !in 0f..100f ||
                            score.midterm !in 0f..100f ||
                            score.final !in 0f..100f ||
                            score.homework !in 0f..100f ||
                            score.participation !in 0f..100f
                }) {
                Toast.makeText(requireContext(), "All scores must be between 0 and 100", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            Dialog.showDialog(
                requireContext(),
                title = "Submit Scores",
                description = "Are you sure you want to submit these scores?",
                onBtnClick = {
                    viewModel.submitScores(currentScores, year, semester)
                }
            )
        }
    }

    private fun fetchScores() {
        val year = binding.yearSpinner.selectedItem.toString().toInt()
        val semester = binding.semesterSpinner.selectedItem.toString().toInt()
        viewModel.fetchStudents(year, semester)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.submitScoresBtn.isEnabled = !state.isLoading

                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }

                if (state.submitSuccess) {
                    fetchScores()
                    Toast.makeText(requireContext(), "Scores submitted successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.studentScores.observe(viewLifecycleOwner) { scores ->
            scoreAdapter.submitList(scores)
            updateAverageScore(scores)
            binding.submitScoresBtn.isEnabled = scores.isNotEmpty()
        }
    }

    private fun updateAverageScore(scores: List<StudentScore>) {
        val average = scores.map { it.total }.average().takeIf { !it.isNaN() } ?: 0.0
        binding.averageScoreText.text = getString(R.string.class_average).format(average)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}