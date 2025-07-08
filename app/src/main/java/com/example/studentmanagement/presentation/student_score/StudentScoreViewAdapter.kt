package com.example.studentmanagement.presentation.student_score

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.databinding.ItemStudentScoreViewBinding

class StudentScoreViewAdapter :
    ListAdapter<ScoreItem, StudentScoreViewAdapter.ScoreViewHolder>(ScoreDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val binding = ItemStudentScoreViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ScoreViewHolder(
        private val binding: ItemStudentScoreViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(score: ScoreItem) {
            binding.apply {
                yearSemesterText.text = "${score.year} - ${score.semester}"
                assignmentScoreText.text = "%.1f".format(score.assignment)
                homeworkScoreText.text = "%.1f".format(score.homework)
                midtermScoreText.text = "%.1f".format(score.midterm)
                finalScoreText.text = "%.1f".format(score.final)
                participationScoreText.text = "%.1f".format(score.participation)
                totalScoreText.text = "%.1f".format(score.total)
            }
        }
    }

    private class ScoreDiffCallback : DiffUtil.ItemCallback<ScoreItem>() {
        override fun areItemsTheSame(oldItem: ScoreItem, newItem: ScoreItem): Boolean {
            return oldItem.year == newItem.year && oldItem.semester == newItem.semester
        }

        override fun areContentsTheSame(oldItem: ScoreItem, newItem: ScoreItem): Boolean {
            return oldItem == newItem
        }
    }
}