package com.example.studentmanagement.presentation.teacher_attendance.components

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.presentation.teacher_attendance.AttendanceStatus
import com.example.studentmanagement.presentation.teacher_attendance.StudentAttendance
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

class AttendanceAdapter(
    private val onStatusChanged: (String, AttendanceStatus) -> Unit,
    private val onPermissionAction: (String, Boolean) -> Unit
) : ListAdapter<StudentAttendance, AttendanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_attendance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewStudentName: TextView = view.findViewById(R.id.textViewStudentName)
        private val chipGroupStatus: ChipGroup = view.findViewById(R.id.chipGroupStatus)
        private val chipPresent: Chip = view.findViewById(R.id.chipPresent)
        private val chipAbsent: Chip = view.findViewById(R.id.chipAbsent)
        private val chipPermissionRequest: Chip = view.findViewById(R.id.chipPermissionRequest)
        private val containerNormalStatus: View = view.findViewById(R.id.containerNormalStatus)

        fun bind(item: StudentAttendance) {
            textViewStudentName.text = item.fullName

            when {
                item.hasPermissionRequest -> {
                    containerNormalStatus.visibility = View.GONE
                    chipPermissionRequest.visibility = View.VISIBLE
                    chipPermissionRequest.setOnClickListener {
                        showPermissionRequestDialog(item)
                    }
                }

                else -> {
                    containerNormalStatus.visibility = View.VISIBLE
                    chipPermissionRequest.visibility = View.GONE
                    setupNormalAttendance(item)
                }
            }
        }

        private fun setupNormalAttendance(item: StudentAttendance) {
            chipGroupStatus.setOnCheckedStateChangeListener(null)

            when {
                item.status == AttendanceStatus.PERMISSION ||
                        (item.statusModified && item.hasPermissionRequest && item.status == AttendanceStatus.ABSENT) -> {
                    if (item.status == AttendanceStatus.PERMISSION) {
                        chipAbsent.visibility = View.GONE
                        chipPresent.apply {
                            visibility = View.VISIBLE
                            isChecked = true
                            text = "Permission"
                            isEnabled = false
                            alpha = 1.0f
                            setTextColor(ContextCompat.getColor(context, android.R.color.white))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.grade_c)
                            )
                        }
                    } else {
                        chipPresent.visibility = View.GONE
                        chipAbsent.apply {
                            visibility = View.VISIBLE
                            isChecked = true
                            isEnabled = false
                            alpha = 1.0f
                            setTextColor(ContextCompat.getColor(context, android.R.color.white))
                            chipBackgroundColor = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.chip_absent_selected)
                            )
                        }
                    }
                }

                item.isSubmitted -> {
                    when (item.status) {
                        AttendanceStatus.PRESENT -> {
                            chipPresent.apply {
                                isChecked = true
                                alpha = 1.0f
                                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                                chipBackgroundColor = ColorStateList.valueOf(
                                    ContextCompat.getColor(context, R.color.chip_present_selected)
                                )
                            }
                            chipAbsent.visibility = View.GONE
                        }

                        AttendanceStatus.ABSENT -> {
                            chipAbsent.apply {
                                isChecked = true
                                alpha = 1.0f
                                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                                chipBackgroundColor = ColorStateList.valueOf(
                                    ContextCompat.getColor(context, R.color.chip_absent_selected)
                                )
                            }
                            chipPresent.visibility = View.GONE
                        }

                        else -> {}
                    }
                    setChipsEnabled(false)
                }

                else -> {
                    chipPresent.apply {
                        text = context.getString(R.string.present)
                        visibility = View.VISIBLE
                        isEnabled = true
                        alpha = 1.0f
                    }

                    chipAbsent.apply {
                        visibility = View.VISIBLE
                        isEnabled = true
                        alpha = 1.0f
                    }

                    setupChipColorStateList(
                        chipPresent,
                        R.color.chip_present_selected,
                        R.color.chip_default_background
                    )
                    setupChipColorStateList(
                        chipAbsent,
                        R.color.chip_absent_selected,
                        R.color.chip_default_background
                    )

                    when {
                        item.statusModified && item.status == AttendanceStatus.PRESENT -> {
                            chipPresent.isChecked = true
                            chipAbsent.isChecked = false
                        }

                        item.statusModified && item.status == AttendanceStatus.ABSENT -> {
                            chipAbsent.isChecked = true
                            chipPresent.isChecked = false
                        }

                        else -> {
                            chipPresent.isChecked = false
                            chipAbsent.isChecked = false
                        }
                    }

                    chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
                        val checkedId = checkedIds.firstOrNull()
                        when (checkedId) {
                            R.id.chipPresent -> {
                                onStatusChanged(item.studentId, AttendanceStatus.PRESENT)
                            }

                            R.id.chipAbsent -> {
                                onStatusChanged(item.studentId, AttendanceStatus.ABSENT)
                            }

                            null -> {}
                        }
                    }
                }
            }

            if (!item.statusModified && !item.isSubmitted) {
                chipGroupStatus.clearCheck()
            }
        }

        private fun setupChipColorStateList(chip: Chip, selectedColor: Int, defaultColor: Int) {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val colors = intArrayOf(
                ContextCompat.getColor(chip.context, selectedColor),
                ContextCompat.getColor(chip.context, defaultColor)
            )
            chip.chipBackgroundColor = ColorStateList(states, colors)

            val textStates = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val textColors = intArrayOf(
                ContextCompat.getColor(chip.context, android.R.color.white),
                ContextCompat.getColor(chip.context, android.R.color.black)
            )
            chip.setTextColor(ColorStateList(textStates, textColors))
        }

        private fun setChipsEnabled(enabled: Boolean) {
            chipPresent.isEnabled = enabled
            chipAbsent.isEnabled = enabled

            chipPresent.alpha = if (enabled) 1.0f else 0.6f
            chipAbsent.alpha = if (enabled) 1.0f else 0.6f
        }

        private fun showPermissionRequestDialog(item: StudentAttendance) {
            MaterialAlertDialogBuilder(itemView.context).setView(R.layout.dialog_permission_request)
                .show().apply {
                    findViewById<TextView>(R.id.textViewStudentName)?.text = item.fullName
                    findViewById<TextView>(R.id.textViewRequestDate)?.text = "Date: ${item.date}"
                    findViewById<TextView>(R.id.textViewReason)?.text = item.permissionReason

                    findViewById<MaterialButton>(R.id.buttonApprove)?.setOnClickListener {
                        onPermissionAction(item.studentId, true)
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.buttonReject)?.setOnClickListener {
                        onPermissionAction(item.studentId, false)
                        dismiss()
                    }
                }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<StudentAttendance>() {
        override fun areItemsTheSame(
            oldItem: StudentAttendance, newItem: StudentAttendance
        ): Boolean = oldItem.studentId == newItem.studentId

        override fun areContentsTheSame(
            oldItem: StudentAttendance, newItem: StudentAttendance
        ): Boolean = oldItem == newItem
    }
}