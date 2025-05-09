package com.example.studentmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentViewModel(private var students: List<Student>) :
    RecyclerView.Adapter<StudentViewModel.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.studentName)
        val email: TextView = itemView.findViewById(R.id.studentEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_card, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.name.text = student.fullName
        holder.email.text = student.email
    }

    override fun getItemCount(): Int = students.size

    fun updateList(newList: List<Student>) {
        students = newList
        notifyDataSetChanged()
    }
}


data class Student(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val studentId: String = "",
    val phone: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName"
}
