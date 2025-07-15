package com.example.studentmanagement.presentation.subject_detail.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.databinding.ItemSubjectDetailHeaderBinding
import com.example.studentmanagement.databinding.ItemSubjectDetailRowBinding
import com.example.studentmanagement.presentation.subject_detail.SubjectDetailItem

/**
 * @Author: John Youlong.
 * @Date: 7/15/25.
 * @Email: johnyoulong@gmail.com.
 */

class SubjectDetailAdapter :
    ListAdapter<SubjectDetailItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ROW = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SubjectDetailItem.SectionHeader -> TYPE_HEADER
            is SubjectDetailItem.DetailRow -> TYPE_ROW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemSubjectDetailHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            TYPE_ROW -> {
                val binding = ItemSubjectDetailRowBinding.inflate(inflater, parent, false)
                RowViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val item = getItem(position) as SubjectDetailItem.SectionHeader
                holder.bind(item)
            }

            is RowViewHolder -> {
                val item = getItem(position) as SubjectDetailItem.DetailRow
                holder.bind(item)
            }
        }
    }

    class HeaderViewHolder(private val binding: ItemSubjectDetailHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(header: SubjectDetailItem.SectionHeader) {
            binding.textSectionTitle.text = header.title
        }
    }

    class RowViewHolder(private val binding: ItemSubjectDetailRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: SubjectDetailItem.DetailRow) {
            binding.apply {
                textLabel.text = row.label
                textValue.text = row.value
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SubjectDetailItem>() {
        override fun areItemsTheSame(
            oldItem: SubjectDetailItem,
            newItem: SubjectDetailItem
        ): Boolean {
            return when {
                oldItem is SubjectDetailItem.SectionHeader && newItem is SubjectDetailItem.SectionHeader -> {
                    oldItem.title == newItem.title
                }

                oldItem is SubjectDetailItem.DetailRow && newItem is SubjectDetailItem.DetailRow -> {
                    oldItem.label == newItem.label
                }

                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: SubjectDetailItem,
            newItem: SubjectDetailItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}