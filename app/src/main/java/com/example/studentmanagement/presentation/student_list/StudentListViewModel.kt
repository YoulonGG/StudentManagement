package com.example.studentmanagement.presentation.student_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.studentmanagement.R
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.StudentResponse
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @Author: John Youlong.
 * @Date: 6/2/25.
 * @Email: johnyoulong@gmail.com.
 */

class StudentListViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<StudentListAction, StudentListUiState>() {


    private var allStudents: List<StudentResponse> = emptyList()
    val adapter = StudentAdapter()

    override fun setInitialState(): StudentListUiState = StudentListUiState()

    override fun onAction(event: StudentListAction) {
        when (event) {
            StudentListAction.StudentList -> {
                getStudentsList()
            }

            is StudentListAction.SearchStudents -> {
                searchStudents(event.query)
            }
        }
    }

    private fun searchStudents(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(allStudents)
            setState { copy(student = allStudents) }
            return
        }

        val filteredList = allStudents.filter { student ->
            student.name?.contains(query, ignoreCase = true) == true
        }

        adapter.submitList(filteredList)
        setState { copy(student = filteredList) }
    }

    private fun getStudentsList() {
        setState { copy(isLoading = true) }

        firestore
            .collection("students")
            .get()
            .addOnSuccessListener { result ->
                allStudents =
                    result.documents.mapNotNull { it.toObject(StudentResponse::class.java) }
                adapter.submitList(allStudents)
                setState {
                    copy(
                        isLoading = false,
                        student = allStudents
                    )
                }
            }
            .addOnFailureListener { e ->
                setState {
                    copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
    }

    inner class StudentAdapter :
        RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

        private var students: List<StudentResponse> = emptyList()

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

        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            val student = students[position]
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

        override fun getItemCount(): Int = students.size

        fun submitList(newList: List<StudentResponse>) {
            val diffCallback = object : DiffUtil.Callback() {
                override fun getOldListSize() = students.size
                override fun getNewListSize() = newList.size
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    students[oldItemPosition].studentID == newList[newItemPosition].studentID

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    students[oldItemPosition] == newList[newItemPosition]
            }
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            students = newList
            diffResult.dispatchUpdatesTo(this)
        }

    }
}


data class StudentListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val student: List<StudentResponse?> = emptyList()
)


sealed class StudentListAction {
    data object StudentList : StudentListAction()
    data class SearchStudents(val query: String) : StudentListAction()
}
