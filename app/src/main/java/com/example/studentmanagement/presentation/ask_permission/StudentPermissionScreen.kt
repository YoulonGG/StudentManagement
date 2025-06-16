package com.example.studentmanagement.presentation.ask_permission

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentAskPermissionScreenBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class StudentPermissionFragment : Fragment(R.layout.fragment_ask_permission_screen) {
    private lateinit var binding: FragmentAskPermissionScreenBinding
    private val viewModel: StudentPermissionViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAskPermissionScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeState()
    }

    private fun setupViews() {
        binding.apply {
            buttonSelectDate.setOnClickListener {
                showDatePicker()
            }

            editTextReason.addTextChangedListener {
                viewModel.onAction(StudentPermissionEvent.SetReason(it?.toString() ?: ""))
            }

            buttonSubmitRequest.setOnClickListener {
                viewModel.onAction(StudentPermissionEvent.SubmitRequest)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                viewModel.onAction(StudentPermissionEvent.SetDate(date))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                with(binding) {
                    buttonSelectDate.text = state.selectedDate.ifBlank { "Select Date" }
                    buttonSubmitRequest.isEnabled = !state.isLoading

                    state.error?.let { error ->
                        showError(error)
                        viewModel.onAction(StudentPermissionEvent.ClearError)
                    }

                    if (state.success) {
                        showSuccess()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess() {
        Toast.makeText(
            requireContext(),
            "Permission request submitted successfully",
            Toast.LENGTH_SHORT
        ).show()
    }
}