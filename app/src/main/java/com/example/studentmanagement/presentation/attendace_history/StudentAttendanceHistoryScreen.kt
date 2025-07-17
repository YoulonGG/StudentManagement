package com.example.studentmanagement.presentation.attendace_history

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
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
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        val goBack = view.findViewById<ImageView>(R.id.goBack)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)

        toolbarTitle.text = getString(R.string.attendance_record)
        goBack.setOnClickListener { findNavController().navigateUp() }
        binding.textViewSelectedMonth.setOnClickListener { showCustomMonthYearPicker() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
        viewModel.onAction(AttendanceHistoryEvent.LoadMonthStats(currentMonth))
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

    private fun updateUI(state: AttendanceHistoryState) {
        binding.apply {
            progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            tableLayout.visibility = if (state.isLoading) View.INVISIBLE else View.VISIBLE

            textViewSelectedMonth.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(
                    SimpleDateFormat("yyyy-MM", Locale.getDefault())
                        .parse(state.selectedMonth) ?: Date()
                )

            tableLayout.removeAllViews()

            val headerRow = TableRow(requireContext()).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    resources.getDimensionPixelSize(R.dimen.table_row_height)
                )
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.tool_bar_background
                    )
                )
            }

            addTableCell(
                headerRow,
                "Student Name",
                width = 210,
                textColor = Color.WHITE,
                isHeader = true
            )
            addTableCell(
                headerRow,
                "Present",
                width = 120,
                textColor = Color.WHITE,
                isHeader = true
            )
            addTableCell(headerRow, "Absent", width = 120, textColor = Color.WHITE, isHeader = true)
            addTableCell(
                headerRow,
                "Permission",
                width = 140,
                textColor = Color.WHITE,
                isHeader = true
            )

            tableLayout.addView(headerRow)

            state.monthlyStats.forEachIndexed { _, stats ->
                val row = TableRow(requireContext()).apply {
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        resources.getDimensionPixelSize(R.dimen.table_row_height)
                    )
                    setBackgroundResource(R.drawable.table_cell_background)
                }

                addTableCell(row, stats.studentName, width = 200)
                addTableCell(
                    row,
                    stats.presentCount.toString(),
                    width = 80,
                    textColor = "#43A047".toColorInt()
                )
                addTableCell(
                    row,
                    stats.absentCount.toString(),
                    width = 80,
                    textColor = "#E53935".toColorInt()
                )
                addTableCell(
                    row,
                    stats.permissionCount.toString(),
                    width = 100,
                    textColor = "#FB8C00".toColorInt()
                )

                tableLayout.addView(row)
            }

            state.error?.let {
                Dialog.showDialog(
                    requireContext(),
                    title = "Error",
                    description = it,
                    onBtnClick = {
                        viewModel.onAction(AttendanceHistoryEvent.ClearError)
                    }
                )
            }
        }
    }

    private fun addTableCell(
        row: TableRow,
        text: String,
        width: Int, // Width in dp
        gravity: Int = Gravity.CENTER,
        textColor: Int? = null,
        isHeader: Boolean = false
    ) {
        TextView(requireContext()).apply {
            // Convert dp to pixels for consistent sizing
            val widthInPx = (width * resources.displayMetrics.density).toInt()
            layoutParams = TableRow.LayoutParams(widthInPx, TableRow.LayoutParams.MATCH_PARENT)

            this.text = text
            this.gravity = gravity
            setPadding(16, 30, 16, 30) // Added horizontal padding for better readability
            textSize = if (isHeader) 14f else 12f
            if (isHeader) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            if (textColor != null) {
                setTextColor(textColor)
            } else {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

            // Don't add background to header cells since the row has the background
            if (!isHeader) {
                setBackgroundResource(R.drawable.table_cell_background)
            }

            // Ensure single line to prevent cell height issues
            setSingleLine(true)

            row.addView(this)
        }
    }
}