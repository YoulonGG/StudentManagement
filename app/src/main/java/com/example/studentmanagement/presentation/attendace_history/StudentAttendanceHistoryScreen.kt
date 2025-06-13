package com.example.studentmanagement.presentation.attendace_history

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.FragmentStudentAttendanceHistoryScreenBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class StudentAttendanceHistoryScreen :
    Fragment(R.layout.fragment_student_attendance_history_screen) {
    private lateinit var binding: FragmentStudentAttendanceHistoryScreenBinding
    private val viewModel: StudentAttendanceViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStudentAttendanceHistoryScreenBinding.bind(view)

        setupViews()
        observeState()

        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        viewModel.onAction(AttendanceHistoryEvent.LoadMonthStats(currentMonth))
    }

    private fun setupViews() {
        binding.textViewSelectedMonth.setOnClickListener {
            showCustomMonthYearPicker()
        }
    }

    private fun showCustomMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val dialog = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_month_year_picker, null)

        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        monthPicker.apply {
            minValue = 0
            maxValue = 11
            value = calendar.get(Calendar.MONTH)
            displayedValues = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
        }

        yearPicker.apply {
            minValue = 2000
            maxValue = calendar.get(Calendar.YEAR)
            value = calendar.get(Calendar.YEAR)
        }

        dialog.setView(dialogView)
            .setTitle("Select Month")
            .setPositiveButton("OK") { _, _ ->
                val monthYear = String.format(
                    Locale.getDefault(),
                    "%04d-%02d",
                    yearPicker.value,
                    monthPicker.value + 1
                )
                viewModel.onAction(AttendanceHistoryEvent.LoadMonthStats(monthYear))
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun updateUI(state: AttendanceHistoryState) {
        binding.apply {
            loadingContainer.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            scrollViewMonthlyStats.visibility =
                if (state.isLoading) View.INVISIBLE else View.VISIBLE
            textViewSelectedMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(
                    SimpleDateFormat("yyyy-MM", Locale.getDefault())
                        .parse(state.selectedMonth) ?: Date()
                )

            val container = scrollViewMonthlyStats.getChildAt(0) as LinearLayout
            container.removeAllViews()

            state.monthlyStats.forEach { stats ->
                val itemView = layoutInflater.inflate(
                    R.layout.item_monthly_attendance,
                    container,
                    false
                )

                itemView.apply {
                    findViewById<TextView>(R.id.textViewName).text = stats.studentName
                    findViewById<TextView>(R.id.textViewTotal).text = stats.totalDays.toString()
                    findViewById<TextView>(R.id.textViewPresent).text =
                        stats.presentCount.toString()
                    findViewById<TextView>(R.id.textViewAbsent).text = stats.absentCount.toString()
                    findViewById<TextView>(R.id.textViewLate).text = stats.lateCount.toString()
                }

                container.addView(itemView)
            }

            textViewEmptyState.visibility =
                if (state.monthlyStats.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE

            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.onAction(AttendanceHistoryEvent.ClearError)
            }
        }
    }
}