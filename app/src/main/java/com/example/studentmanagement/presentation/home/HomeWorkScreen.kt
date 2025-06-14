package com.example.studentmanagement.presentation.home

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentHomeWorkScreenBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeworkScreen : Fragment(R.layout.fragment_home_work_screen) {
    private val viewModel: HomeworkViewModel by viewModel()
    private var _binding: FragmentHomeWorkScreenBinding? = null
    private val binding get() = _binding!!

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            showTitleDialog { title ->
                viewModel.onAction(HomeworkAction.UploadHomework(title, uri))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeWorkScreenBinding.bind(view)

        setupUI()
        observeViewModel()
        viewModel.onAction(HomeworkAction.LoadHomework)
    }

    private fun setupUI() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = viewModel.adapter
                addItemDecoration(
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                )
            }

            buttonUpload.setOnClickListener {
                pickFileLauncher.launch("application/*")
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.emptyView.isVisible =
                        !state.isLoading && state.homeworkList?.isEmpty() == true

                    state.error?.let { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showTitleDialog(onSubmit: (String) -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Homework Title")
            .setView(R.layout.dialog_homework_title)
            .setPositiveButton("Upload") { dialog, _ ->
                val editText = (dialog as AlertDialog)
                    .findViewById<EditText>(R.id.editTextTitle)
                val title = editText?.text?.toString() ?: return@setPositiveButton
                if (title.isNotBlank()) {
                    onSubmit(title)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}