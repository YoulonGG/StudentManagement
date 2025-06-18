package com.example.studentmanagement.presentation.attendace_history

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.NumberPicker
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
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
            tableLayout.visibility = if (state.isLoading) View.INVISIBLE else View.VISIBLE

            textViewSelectedMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    .parse(state.selectedMonth) ?: Date())

            tableLayout.removeAllViews()

            val headerRow = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }

            addTableCell(headerRow, "Student Name", weight = 2f, textColor = Color.WHITE)
//            addTableCell(headerRow, "Total", weight = 0.5f, textColor = Color.WHITE)
            addTableCell(headerRow, "Present", weight = 1f, textColor = Color.WHITE)
            addTableCell(headerRow, "Absent", weight = 1f, textColor = Color.WHITE)
            addTableCell(headerRow, "Permission", weight = 1f, textColor = Color.WHITE)

            tableLayout.addView(headerRow)

            state.monthlyStats.forEach { stats ->
                val row = TableRow(requireContext()).apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundColor(Color.WHITE)
                }

                addTableCell(row, stats.studentName, weight = 2f)
//                addTableCell(row, stats.totalDays.toString(), weight = 0.5f)
                addTableCell(row, stats.presentCount.toString(), weight = 1f, textColor = "#43A047".toColorInt())
                addTableCell(row, stats.absentCount.toString(), weight = 1f, textColor = "#E53935".toColorInt())
                addTableCell(row, stats.permissionCount.toString(), weight = 1f, textColor = "#FB8C00".toColorInt())

                tableLayout.addView(row)

                val divider = View(requireContext()).apply {
                    layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1)
                    setBackgroundColor("#E0E0E0".toColorInt())
                }
                tableLayout.addView(divider)
            }

            textViewEmptyState.visibility =
                if (state.monthlyStats.isEmpty() && !state.isLoading) View.VISIBLE else View.GONE

            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.onAction(AttendanceHistoryEvent.ClearError)
            }
        }
    }

    private fun addTableCell(
        row: TableRow,
        text: String,
        weight: Float,
        gravity: Int = Gravity.CENTER,
        textColor: Int? = null
    ) {
        TextView(requireContext()).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT).apply {
                this.weight = weight
            }
            this.text = text
            this.gravity = gravity
            setPadding(0, 10, 0, 10)
            textSize = 14f
            if (textColor != null) {
                setTextColor(textColor)
            }
            row.addView(this)
        }
    }
}