package com.example.studentmanagement.presentation.teacher_submit_score

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.databinding.ItemStudentScoreBinding


class StudentScoreAdapter :
    ListAdapter<StudentScore, StudentScoreAdapter.ViewHolder>(ScoreDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentScoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemStudentScoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val scoreWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateScore()
            }
        }

        init {
            binding.assignmentScore.addTextChangedListener(scoreWatcher)
            binding.homeworkScore.addTextChangedListener(scoreWatcher)
            binding.midtermScore.addTextChangedListener(scoreWatcher)
            binding.finalScore.addTextChangedListener(scoreWatcher)
        }

        fun bind(item: StudentScore) {
            binding.apply {
                studentNameText.text = item.name
                assignmentScore.setText(
                    if (item.assignment == 0f) "0" else item.assignment.toInt().toString()
                )
                homeworkScore.setText(
                    if (item.homework == 0f) "0" else item.homework.toInt().toString()
                )
                midtermScore.setText(
                    if (item.midterm == 0f) "0" else item.midterm.toInt().toString()
                )
                finalScore.setText(if (item.final == 0f) "0" else item.final.toInt().toString())
                totalScore.text = item.total.toInt().toString()
                averageScore.text = ((item.total / 400) * 100).toInt().toString()
            }
        }

        private fun updateScore() {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = getItem(position)
                binding.apply {
                    item.assignment = assignmentScore.text.toString().toFloatOrNull() ?: 0f
                    item.homework = homeworkScore.text.toString().toFloatOrNull() ?: 0f
                    item.midterm = midtermScore.text.toString().toFloatOrNull() ?: 0f
                    item.final = finalScore.text.toString().toFloatOrNull() ?: 0f
                    totalScore.text = item.total.toInt().toString()
                    averageScore.text = ((item.total / 400) * 100).toInt().toString()
                }
            }
        }
    }
}

class ScoreDiffCallback : DiffUtil.ItemCallback<StudentScore>() {
    override fun areItemsTheSame(oldItem: StudentScore, newItem: StudentScore): Boolean {
        return oldItem.studentId == newItem.studentId
    }

    override fun areContentsTheSame(oldItem: StudentScore, newItem: StudentScore): Boolean {
        return oldItem == newItem
    }
}