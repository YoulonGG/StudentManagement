package com.example.studentmanagement.presentation.subjects_list

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.databinding.FragmentSubjectListScreenBinding
import com.example.studentmanagement.presentation.subjects_list.components.CreateSubjectBottomSheet
import com.example.studentmanagement.presentation.subjects_list.components.SubjectAdapter
import com.example.studentmanagement.presentation.subjects_list.components.SwipeToDeleteCallback
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
        val backButton = view.findViewById<ImageView>(R.id.goBack)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        subjectTitle.text = "Subjects"

        checkPermissions()

        subjectAdapter = SubjectAdapter { subject ->
            Log.e("SubjectListFragment", "Subject clicked: ${subject.id}")
            findNavController().navigate(
                R.id.navigate_subject_to_subject_details,
                bundleOf("subjectId" to subject.id)
            )
        }

        binding.apply {
            recyclerViewSubjects.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = subjectAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        requireContext(), DividerItemDecoration.VERTICAL
                    )
                )

                val swipeToDeleteCallback = SwipeToDeleteCallback(requireContext()) { position ->
                    val subject = subjectAdapter.currentList[position]
                    Dialog.showTwoButtonDialog(
                        requireContext(),
                        title = "Delete Subject",
                        description = "Are you sure you want to delete '${subject.name}'?",
                        positiveButtonText = "Delete",
                        negativeButtonText = "Cancel",
                        onPositiveClick = {
                            subjectAdapter.notifyDataSetChanged()
                            viewModel.onAction(SubjectListEvent.DeleteSubject(subject.id))
                        },
                        onNegativeClick = {
                            subjectAdapter.notifyDataSetChanged()
                        })
                }
                val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
                itemTouchHelper.attachToRecyclerView(this)
            }

            fabCreateSubject.setOnClickListener {
                showCreateSubjectBottomSheet()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
        viewModel.onAction(SubjectListEvent.LoadSubjects)
    }

    private fun showCreateSubjectBottomSheet() {
        val bottomSheet = CreateSubjectBottomSheet()
        bottomSheet.setOnSubjectCreatedListener { subjectData ->
            viewModel.onAction(
                SubjectListEvent.CreateSubject(
                    name = subjectData.name,
                    description = subjectData.description,
                    code = subjectData.code,
                    className = subjectData.className,
                    classTime = subjectData.classTime,
                    imageUri = subjectData.imageUri
                )
            )
        }
        bottomSheet.show(parentFragmentManager, "CreateSubjectBottomSheet")
    }

    private fun updateUI(state: SubjectListState) {
        binding.apply {
            progressBar.isVisible = state.isLoading
            progressBarCreate.isVisible = state.isCreating
            subjectAdapter.submitList(state.subjects)

            state.error?.let { errorMessage ->
                Dialog.showDialog(
                    requireContext(),
                    title = "Error",
                    description = errorMessage,
                    buttonText = "Okay"
                ) {
                    viewModel.onAction(SubjectListEvent.ClearError)
                }
            }

            state.successMessage?.let { successMessage ->
                Dialog.showDialog(
                    requireContext(),
                    title = "Success",
                    description = successMessage,
                    buttonText = "Okay"
                ) {
                    viewModel.onAction(SubjectListEvent.ClearSuccessMessage)
                }
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001
            )
        }
    }
}