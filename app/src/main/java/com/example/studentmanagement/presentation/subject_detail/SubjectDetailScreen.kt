package com.example.studentmanagement.presentation.subject_detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.databinding.FragmentSubjectDetailScreenBinding
import com.example.studentmanagement.presentation.subject_detail.components.SubjectDetailAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SubjectDetailScreen : Fragment(R.layout.fragment_subject_detail_screen) {
    private val viewModel: SubjectDetailViewModel by viewModel()
    private lateinit var binding: FragmentSubjectDetailScreenBinding
    private lateinit var detailAdapter: SubjectDetailAdapter
    private val args: SubjectDetailScreenArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubjectDetailScreenBinding.bind(view)

        val subjectTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        val backButton = view.findViewById<ImageView>(R.id.goBack)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        subjectTitle.text = getString(R.string.subject_details)

        detailAdapter = SubjectDetailAdapter()

        binding.apply {
            recyclerViewDetails.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = detailAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(), DividerItemDecoration.VERTICAL
                    )
                )
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {
                        progressBar.isVisible = state.isLoading

                        state.subject?.let {
                            detailAdapter.submitList(state.detailItems)
                        }

                        state.error?.let { errorMessage ->
                            Dialog.showDialog(
                                requireContext(),
                                title = "Error",
                                description = errorMessage,
                                buttonText = "Okay"
                            ) {
                                viewModel.onAction(SubjectDetailEvent.ClearError)
                            }
                        }

                        state.successMessage?.let { successMessage ->
                            Dialog.showDialog(
                                requireContext(),
                                title = "Success",
                                description = successMessage,
                                buttonText = "Okay"
                            ) {
                                viewModel.onAction(SubjectDetailEvent.ClearSuccessMessage)
                            }
                        }
                    }
                }
            }
        }
        viewModel.onAction(SubjectDetailEvent.LoadSubjectDetail(args.subjectId))
    }
}