package com.example.studentmanagement.presentation.teacher.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.data.CourseData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class TeacherCourseManagementFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CourseAdapter
    private val courses = mutableListOf<CourseData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teacher_course_management, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        view.findViewById<View>(R.id.fabAddCourse).setOnClickListener {
            showAddEditCourseDialog(null)
        }

        setupRecyclerView(view)
        loadTeacherCourses()

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCourses)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CourseAdapter(
            courses,
            onEditClick = { course -> showAddEditCourseDialog(course) },
            onDeleteClick = { course -> deleteCourse(course) }
        )
        recyclerView.adapter = adapter
    }

    private fun loadTeacherCourses() {
        val teacherId = auth.currentUser?.uid ?: return

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvNoCourses)?.visibility = View.GONE

        db.collection("courses")
            .whereEqualTo("instructorId", teacherId)
            .get()
            .addOnSuccessListener { documents ->
                courses.clear()
                courses.addAll(documents.toObjects(CourseData::class.java))
                adapter.notifyDataSetChanged()
                updateEmptyState(courses.isEmpty())
            }
            .addOnFailureListener {
                updateEmptyState(true)
            }
    }

    private fun showAddEditCourseDialog(course: CourseData?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_course, null)

        if (course != null) {
            dialogView.findViewById<EditText>(R.id.etCourseName).setText(course.name)
            dialogView.findViewById<EditText>(R.id.etCourseCode).setText(course.code)
            dialogView.findViewById<EditText>(R.id.etCourseDescription).setText(course.description)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (course == null) "Add New Course" else "Edit Course")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogView.findViewById<EditText>(R.id.etCourseName).text.toString()
                val code = dialogView.findViewById<EditText>(R.id.etCourseCode).text.toString()
                val description =
                    dialogView.findViewById<EditText>(R.id.etCourseDescription).text.toString()

                if (name.isEmpty() || code.isEmpty()) {
                    Toast.makeText(context, "Name and code are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (course == null) {
                    addCourse(name, code, description)
                } else {
                    updateCourse(course.id, name, code, description)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addCourse(name: String, code: String, description: String) {
        val teacherId = auth.currentUser?.uid ?: return
        val teacherName = auth.currentUser?.displayName ?: "Teacher"

        val course = hashMapOf(
            "name" to name,
            "code" to code,
            "description" to description,
            "instructorId" to teacherId,
            "instructorName" to teacherName,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("courses")
            .add(course)
            .addOnSuccessListener {
                loadTeacherCourses()
                Toast.makeText(context, "Course added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add course", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCourse(courseId: String, name: String, code: String, description: String) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "code" to code,
            "description" to description
        )

        db.collection("courses")
            .document(courseId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                loadTeacherCourses()
                Toast.makeText(context, "Course updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update course", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCourse(course: CourseData) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Delete")
            .setMessage("Delete ${course.name}? This will also remove all enrollments.")
            .setPositiveButton("Delete") { _, _ ->
                // Delete course and all enrollments
                val batch = db.batch()

                // First delete enrollments
                db.collection("enrollments")
                    .whereEqualTo("courseId", course.id)
                    .get()
                    .addOnSuccessListener { enrollments ->
                        for (enrollment in enrollments) {
                            batch.delete(enrollment.reference)

                            // Remove course from student's courses list
                            val studentId = enrollment.getString("studentId") ?: continue
                            batch.update(
                                db.collection("users").document(studentId),
                                "courses", FieldValue.arrayRemove(course.id)
                            )
                        }

                        // Then delete the course
                        batch.delete(db.collection("courses").document(course.id))

                        batch.commit()
                            .addOnSuccessListener {
                                loadTeacherCourses()
                                Toast.makeText(context, "Course deleted", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.tvNoCourses)?.visibility =
            if (isEmpty) View.VISIBLE else View.GONE
    }

    class CourseAdapter(
        private var courses: List<CourseData>,
        private val onEditClick: (CourseData) -> Unit,
        private val onDeleteClick: (CourseData) -> Unit
    ) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

        @SuppressLint("NotifyDataSetChanged")
        fun updateCourses(newCourses: List<CourseData>) {
            this.courses = newCourses
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_teacher_course, parent, false)
            return CourseViewHolder(view)
        }

        override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
            holder.bind(courses[position])
        }

        override fun getItemCount(): Int = courses.size

        inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(course: CourseData) {
                itemView.findViewById<TextView>(R.id.tvCourseName).text = course.name
                itemView.findViewById<TextView>(R.id.tvCourseCode).text = course.code
                itemView.findViewById<TextView>(R.id.tvCourseDescription).text = course.description

                itemView.findViewById<Button>(R.id.btnEditCourse).setOnClickListener {
                    onEditClick(course)
                }

                itemView.findViewById<Button>(R.id.btnDeleteCourse).setOnClickListener {
                    onDeleteClick(course)
                }
            }
        }
    }
}