package com.example.studentmanagement.presentation.subjects_list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
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
import com.example.studentmanagement.presentation.subjects_list.components.CreateSubjectBottomSheet
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class SubjectListFragment : Fragment(R.layout.fragment_subject_list_screen) {
    private val viewModel: SubjectListViewModel by viewModel()
    private lateinit var binding: FragmentSubjectListScreenBinding
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectListScreenBinding.bind(view)

        val subjectTitle = view.findViewById<TextView>(R.id.toolbarTitle)

        subjectTitle.text = "Subjects"

        setupViews()
        observeState()
        viewModel.onAction(SubjectListEvent.LoadSubjects)
        checkPermissions()
    }

    private fun setupViews() {
        subjectAdapter = SubjectAdapter { subject ->
            // Navigate to subject details or score submission
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

            fabCreateSubject.setOnClickListener {
                showCreateSubjectBottomSheet()
            }
        }
    }

    private fun showCreateSubjectBottomSheet() {
        val bottomSheet = CreateSubjectBottomSheet()
        bottomSheet.setOnSubjectCreatedListener { name, description, imageUri ->
            viewModel.onAction(
                SubjectListEvent.CreateSubject(
                    name = name,
                    description = description,
                    imageUri = imageUri
                )
            )
        }
        bottomSheet.show(parentFragmentManager, "CreateSubjectBottomSheet")
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
            progressBarCreate.isVisible = state.isCreating
            subjectAdapter.submitList(state.subjects)


            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.onAction(SubjectListEvent.ClearError)
            }

            state.successMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.onAction(SubjectListEvent.ClearError)
            }
        }
    }

    private fun checkPermissions() {
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
}