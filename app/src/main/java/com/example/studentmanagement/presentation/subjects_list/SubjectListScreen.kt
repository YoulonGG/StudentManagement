package com.example.studentmanagement.presentation.subjects_list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentSubjectListScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SubjectListFragment : Fragment(R.layout.fragment_subject_list_screen) {
    private val viewModel: SubjectListViewModel by viewModel()
    private lateinit var binding: FragmentSubjectListScreenBinding
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectListScreenBinding.bind(view)

        setupViews()
        observeState()
        viewModel.onAction(SubjectListEvent.LoadSubjects)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1001
            )
        }

    }

    private fun setupViews() {
        subjectAdapter = SubjectAdapter { subject ->
//            findNavController().navigate(
//                R.id.navigate_subject_list_to_submit_score,
//                bundleOf("subjectId" to subject.id)
//            )
        }

        binding.apply {
            recyclerViewSubjects.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = subjectAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        DividerItemDecoration.VERTICAL
                    )
                )
            }

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: SubjectListState) {
        binding.apply {
            progressBar.isVisible = state.isLoading
            subjectAdapter.submitList(state.subjects)

            textViewEmptyState.isVisible = state.subjects.isEmpty() && !state.isLoading

            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.onAction(SubjectListEvent.ClearError)
            }
        }
    }
}