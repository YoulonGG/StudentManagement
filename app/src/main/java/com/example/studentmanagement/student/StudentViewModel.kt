package com.example.studentmanagement.student

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R


class StudentViewModel(private var students: List<Student>) :
    RecyclerView.Adapter<StudentViewModel.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val studentName: TextView = itemView.findViewById(R.id.studentName)
        val studentEmail: TextView = itemView.findViewById(R.id.studentEmail)
        val studentId: TextView = itemView.findViewById(R.id.studentId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_card, parent, false)
        return StudentViewHolder(view)
    }

    override fun getItemCount(): Int = students.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.studentName.text = student.fullName
        holder.studentEmail.text = student.email
        holder.studentId.text = student.studentId
    }

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
    constructor() : this("", "", "", "", "")

    val fullName: String
        get() = "$firstName $lastName"
}