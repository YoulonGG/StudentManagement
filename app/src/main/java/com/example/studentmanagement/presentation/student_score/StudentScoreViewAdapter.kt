package com.example.studentmanagement.presentation.student_score

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.ItemStudentScoreDetailBinding

class StudentScoreDetailAdapter :
    ListAdapter<StudentScoreDetail, StudentScoreDetailAdapter.ViewHolder>(ScoreDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentScoreDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(
        private val binding: ItemStudentScoreDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudentScoreDetail, position: Int) {
            binding.apply {
                val backgroundColor = if (position % 2 == 0) {
                    ContextCompat.getColor(root.context, R.color.table_row_even)
                } else {
                    ContextCompat.getColor(root.context, R.color.table_row_odd)
                }
                root.setBackgroundColor(backgroundColor)

                subjectText.text = item.subject
                assignmentScoreText.text = item.assignment.toInt().toString()
                homeworkScoreText.text = item.homework.toInt().toString()
                midtermScoreText.text = item.midterm.toInt().toString()
                finalScoreText.text = item.final.toInt().toString()
                totalScoreText.text = item.total.toInt().toString()

                val percentage = item.percentage
                percentageText.text = String.format("%.1f%%", percentage)

                gradeText.text = item.grade
                setGradeColor(gradeText, item.grade)
            }
        }

        private fun setGradeColor(textView: android.widget.TextView, grade: String) {
            val colorRes = when (grade) {
                "A" -> R.color.grade_a
                "B" -> R.color.grade_b
                "C" -> R.color.grade_c
                "D" -> R.color.grade_d
                "F" -> R.color.grade_f
                else -> R.color.primary
            }
            textView.setTextColor(ContextCompat.getColor(textView.context, colorRes))
        }
    }
}

class ScoreDetailDiffCallback : DiffUtil.ItemCallback<StudentScoreDetail>() {
    override fun areItemsTheSame(
        oldItem: StudentScoreDetail,
        newItem: StudentScoreDetail
    ): Boolean {
        return oldItem.subject == newItem.subject
    }

    override fun areContentsTheSame(
        oldItem: StudentScoreDetail,
        newItem: StudentScoreDetail
    ): Boolean {
        return oldItem == newItem
    }
}