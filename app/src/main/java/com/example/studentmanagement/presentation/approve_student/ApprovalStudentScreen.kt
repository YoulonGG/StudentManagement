package com.example.studentmanagement.presentation.approve_student

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentApprovalStudentScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ApprovalStudentScreen : Fragment(R.layout.fragment_approval_student_screen) {
    private val viewModel: ApprovalStudentViewModel by viewModel()
    private var _binding: FragmentApprovalStudentScreenBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApprovalStudentScreenBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
        viewModel.loadPendingStudents()
    }



    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = viewModel.studentAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    when {
                        state.error != null -> {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                        }

                        state.students != null -> {
                            // Data loaded successfully
                            if (state.students.isEmpty()) {
                                // Handle empty state if needed
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}