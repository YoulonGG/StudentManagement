package com.example.studentmanagement.presentation.student_list

/**
 * @Author: John Youlong.
 * @Date: 7/17/25.
 * @Email: johnyoulong@gmail.com.
 */

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.studentmanagement.R
import com.example.studentmanagement.data.dto.StudentResponse

class StudentPagingAdapter :
    PagingDataAdapter<StudentResponse, StudentPagingAdapter.StudentViewHolder>(STUDENT_COMPARATOR) {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.StudentListName)
        val id: TextView = itemView.findViewById(R.id.StudentListId)
        val img: ImageView = itemView.findViewById(R.id.student_list_image)
        val majoring: TextView = itemView.findViewById(R.id.StudentMajoring)
        val sex: TextView = itemView.findViewById(R.id.StudentListSex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = getItem(position) ?: return
        holder.name.text = student.name ?: "N/A"
        holder.id.text = "ID: ${student.studentID ?: "N/A"}"
        holder.majoring.text = "Majoring: ${student.majoring ?: "N/A"}"
        holder.sex.text = "Sex: ${student.gender ?: "N/A"}"

        val imageUrl = student.imageUrl
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(holder.img.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_place_holder_profile)
                .error(R.drawable.ic_place_holder_profile)
                .circleCrop()
                .transform(CircleCrop())
                .into(holder.img)
        } else {
            holder.img.setImageResource(R.drawable.ic_place_holder_profile)
        }

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("student", student)
            }
            holder.itemView.findNavController()
                .navigate(R.id.navigate_student_list_to_student_details, bundle)
        }
    }

    companion object {
        private val STUDENT_COMPARATOR = object : DiffUtil.ItemCallback<StudentResponse>() {
            override fun areItemsTheSame(oldItem: StudentResponse, newItem: StudentResponse) =
                oldItem.studentID == newItem.studentID

            override fun areContentsTheSame(oldItem: StudentResponse, newItem: StudentResponse) =
                oldItem == newItem
        }
    }
}
