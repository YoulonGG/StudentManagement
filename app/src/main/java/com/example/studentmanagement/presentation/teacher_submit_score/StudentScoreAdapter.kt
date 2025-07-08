package com.example.studentmanagement.presentation.teacher_submit_score

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.databinding.ItemStudentScoreBinding

class StudentScoreAdapter : ListAdapter<StudentScore, StudentScoreAdapter.ViewHolder>(ScoreDiffCallback()) {

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
            binding.participationScore.addTextChangedListener(scoreWatcher)
        }

        fun bind(item: StudentScore) {
            binding.apply {
                studentNameText.text = item.name
                assignmentScore.setText(item.assignment.toString())
                homeworkScore.setText(item.homework.toString())
                midtermScore.setText(item.midterm.toString())
                finalScore.setText(item.final.toString())
                participationScore.setText(item.participation.toString())
                totalScore.text = root.context.getString(R.string.score_format).format(item.total)
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
                    item.participation = participationScore.text.toString().toFloatOrNull() ?: 0f
                    totalScore.text = root.context.getString(R.string.score_format).format(item.total)
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
