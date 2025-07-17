package com.example.studentmanagement.presentation.teacher_submit_score

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

        val goBack = view.findViewById<View>(R.id.goBack)
        val submitScoreTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        submitScoreTitle.text = getString(R.string.submit_scores)
        goBack.setOnClickListener { findNavController().navigateUp() }

        setupRecyclerView()
        setupScrollSynchronization()
        setupSubjectSpinner()
        setupSubmitButton()
        observeViewModel()

        viewModel.fetchSubjects()
    }

    private fun setupRecyclerView() {
        binding.studentScoreRecycler.apply {
            adapter = scoreAdapter
            layoutManager = LinearLayoutManager(requireContext())
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

    private fun setupSubjectSpinner() {
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.subjectSpinner.adapter = adapter
        binding.subjectSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedSubject = parent?.getItemAtPosition(position)?.toString() ?: ""
                    if (selectedSubject.isNotEmpty()) {
                        fetchScores(selectedSubject)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupSubmitButton() {
        binding.submitScoresBtn.setOnClickListener {
            val selectedSubject = binding.subjectSpinner.selectedItem?.toString() ?: ""
            val currentScores = scoreAdapter.currentList

            if (selectedSubject.isEmpty()) {
                Dialog.showDialog(
                    requireContext(),
                    title = "No Subject Selected",
                    description = "Please select a subject to submit scores.",
                    onBtnClick = {}
                )
                return@setOnClickListener
            }

            if (currentScores.isEmpty()) {
                Toast.makeText(requireContext(), "No scores to submit", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentScores.any { score ->
                    score.assignment !in 0f..100f ||
                            score.midterm !in 0f..100f ||
                            score.final !in 0f..100f ||
                            score.homework !in 0f..100f
                }) {
                Dialog.showDialog(
                    requireContext(),
                    title = "Invalid Scores",
                    description = "All scores must be between 0 and 100.",
                    onBtnClick = {}
                )
                return@setOnClickListener
            }

            Dialog.showDialog(
                requireContext(),
                title = "Submit Scores",
                description = "Are you sure you want to submit these scores ?",
                onBtnClick = {
                    viewModel.submitScores(currentScores, selectedSubject)
                }
            )
        }
    }

    private fun fetchScores(subject: String) {
        viewModel.fetchStudentsBySubject(subject)
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
                    val selectedSubject = binding.subjectSpinner.selectedItem?.toString() ?: ""
                    fetchScores(selectedSubject)
                    Dialog.showDialog(
                        requireContext(),
                        title = "Success",
                        description = "Scores have been successfully submitted.",
                        onBtnClick = {
                            findNavController().navigateUp()
                        }
                    )
                }
            }
        }

        viewModel.studentScores.observe(viewLifecycleOwner) { scores ->
            scoreAdapter.submitList(scores)
            updateAverageScore(scores)
            binding.submitScoresBtn.isEnabled = scores.isNotEmpty()
        }

        viewModel.subjects.observe(viewLifecycleOwner) { subjects ->
            val adapter = binding.subjectSpinner.adapter as ArrayAdapter<String>
            adapter.clear()
            adapter.addAll(subjects)
            adapter.notifyDataSetChanged()
        }
    }

    private fun updateAverageScore(scores: List<StudentScore>) {
        val average = scores.map { it.total }.average().takeIf { !it.isNaN() } ?: 0
//        binding.averageScoreText.text = getString(R.string.class_average).format(average)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}