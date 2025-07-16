package com.example.studentmanagement.presentation.subjects_list.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.studentmanagement.R
import com.example.studentmanagement.data.dto.Subject

class SubjectAdapter(
    private val onSubjectClicked: (Subject) -> Unit
) : ListAdapter<Subject, SubjectAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val textViewName: TextView = view.findViewById(R.id.textViewSubjectName)
        private val imageViewSubject: ImageView = view.findViewById(R.id.imageViewSubjectIcon)
        private val textViewSubjectDescription: TextView =
            view.findViewById(R.id.textViewSubjectDes)
        private val root: View = view.findViewById(R.id.root)

        fun bind(subject: Subject) {
            textViewName.text = subject.name
            textViewSubjectDescription.text = subject.description ?: "No description available"

            Glide.with(imageViewSubject.context)
                .load(subject.imageUrl ?: subject.localImageUri)
                .circleCrop()
                .into(imageViewSubject)

            root.setOnClickListener { onSubjectClicked(subject) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Subject>() {
        override fun areItemsTheSame(oldItem: Subject, newItem: Subject): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Subject, newItem: Subject): Boolean =
            oldItem == newItem
    }
}