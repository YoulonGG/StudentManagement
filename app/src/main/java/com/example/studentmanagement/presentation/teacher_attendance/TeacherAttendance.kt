package com.example.studentmanagement.presentation.teacher_attendance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import com.example.studentmanagement.databinding.FragmentTeacherAttendanceBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class TeacherAttendanceFragment : Fragment(R.layout.fragment_teacher_attendance) {
    private lateinit var binding: FragmentTeacherAttendanceBinding
    private val viewModel: TeacherAttendanceViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTeacherAttendanceBinding.bind(view)
        val goBack = view.findViewById<ImageView>(R.id.goBack)
        val teacherProfileToolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)

        teacherProfileToolbarTitle.text = getString(R.string.report_attendance)

        goBack.setOnClickListener { findNavController().navigateUp() }

        binding.recyclerViewAttendance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = viewModel.attendanceAdapter
            addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        binding.textViewSelectedDate.setOnClickListener { showDatePicker() }

        binding.buttonSubmitAttendance.setOnClickListener {
            viewModel.onAction(TeacherAttendanceEvent.SubmitAttendance)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.apply {

                        textViewSelectedDate.text = state.selectedDate
                        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                        binding.buttonSubmitAttendance.apply {
                            isEnabled = !state.isSubmitting &&
                                    state.students.isNotEmpty() &&
                                    !state.attendanceSubmitted

                            text = when {
                                state.attendanceSubmitted -> "Already Submitted"
                                state.isSubmitting -> "Submitting..."
                                else -> "Submit Attendance"
                            }

                            alpha = if (isEnabled) 1.0f else 0.6f
                        }

                        buttonSubmitAttendance.isEnabled = !state.isSubmitting &&
                                state.students.isNotEmpty() &&
                                !state.attendanceSubmitted


                        buttonSubmitAttendance.text = when {
                            state.isSubmitting -> "Submitting..."
                            state.attendanceSubmitted -> "Submitted"
                            else -> "Submit Attendance"
                        }

                        textViewStudentCount.text = "Students: ${state.students.size}"

                        state.error?.let {
                            Dialog.showDialog(
                                requireContext(),
                                title = "Error",
                                description = it,
                                onBtnClick = {
                                    viewModel.onAction(TeacherAttendanceEvent.ClearError)
                                }
                            )
                        }

                        if (state.submissionSuccess) {
                            Dialog.showDialog(
                                requireContext(),
                                title = "Success",
                                description = "Attendance saved successfully!",
                                onBtnClick = {
                                    viewModel.onAction(TeacherAttendanceEvent.ClearSuccess)
                                }
                            )
                        }
                    }
                }
            }
        }
        viewModel.onAction(TeacherAttendanceEvent.LoadStudents)
        viewModel.onAction(
            TeacherAttendanceEvent.SetDate(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
        )
    }

    private fun showDatePicker() {
        val currentDateStr = viewModel.uiState.value.selectedDate
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            calendar.time = dateFormat.parse(currentDateStr) ?: Date()
        } catch (e: Exception) {
            Dialog.showDialog(
                requireContext(),
                title = "Error",
                description = e.message ?: "",
                onBtnClick = {}
            )
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate =
                    String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                viewModel.onAction(TeacherAttendanceEvent.SetDate(selectedDate))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = today.timeInMillis

        datePickerDialog.show()
    }
}
