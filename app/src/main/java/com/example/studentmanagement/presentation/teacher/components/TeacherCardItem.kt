package com.example.studentmanagement.presentation.teacher.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.presentation.teacher.HomeCardItem

class TeacherHomeCardAdapter(
    private val items: List<HomeCardItem>,
) : RecyclerView.Adapter<TeacherHomeCardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconImageView: ImageView = view.findViewById(R.id.cardIcon)
        val titleTextView: TextView = view.findViewById(R.id.cardTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.teacher_home_card_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.iconImageView.setImageResource(item.icon)
        holder.titleTextView.text = item.title
        holder.itemView.setOnClickListener { item.onClick() }
    }

    override fun getItemCount() = items.size
}


