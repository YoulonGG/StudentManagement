package com.example.studentmanagement.presentation.teacher.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TeacherStudentsFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: StudentAdapter
    private val students = mutableListOf<UserData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_students, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadStudents()
    }

    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvStudents)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = StudentAdapter(students) { student ->
            showStudentActionsDialog(student)
        }
        recyclerView?.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadStudents() {
        val teacherId = auth.currentUser?.uid ?: return

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvNoStudents)?.visibility = View.GONE

        // First get courses taught by this teacher
        db.collection("courses")
            .whereEqualTo("instructorId", teacherId)
            .get()
            .addOnSuccessListener { courses ->
                if (courses.isEmpty) {
                    updateEmptyState(true)
                    return@addOnSuccessListener
                }

                val courseIds = courses.map { it.id }

                // Then get enrollments in these courses
                db.collection("enrollments")
                    .whereIn("courseId", courseIds)
                    .get()
                    .addOnSuccessListener { enrollments ->
                        if (enrollments.isEmpty) {
                            updateEmptyState(true)
                            return@addOnSuccessListener
                        }

                        val studentIds =
                            enrollments.map { it.getString("studentId") ?: "" }.distinct()

                        // Finally get student details
                        db.collection("users")
                            .whereIn("id", studentIds)
                            .get()
                            .addOnSuccessListener { studentsSnapshot ->
                                students.clear()
                                students.addAll(studentsSnapshot.toObjects(UserData::class.java))
                                adapter.notifyDataSetChanged()
                                updateEmptyState(students.isEmpty())
                            }
                    }
            }
            .addOnFailureListener {
                updateEmptyState(true)
            }
    }

    private fun showStudentActionsDialog(student: UserData) {
        AlertDialog.Builder(requireContext())
            .setTitle(student.name)
            .setMessage("ID: ${student.studentId}\nDepartment: ${student.department}")
            .setPositiveButton("View Grades") { _, _ ->
                // Could navigate to student grades
            }
            .setNeutralButton("Remove from Course") { _, _ ->
                showRemoveFromCourseDialog(student)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRemoveFromCourseDialog(student: UserData) {
        val teacherId = auth.currentUser?.uid ?: return

        db.collection("courses")
            .whereEqualTo("instructorId", teacherId)
            .get()
            .addOnSuccessListener { courses ->
                val courseNames = courses.map { it.getString("name") ?: "Unknown Course" }

                AlertDialog.Builder(requireContext())
                    .setTitle("Remove from Course")
                    .setItems(courseNames.toTypedArray()) { _, which ->
                        val courseId = courses.documents[which].id
                        removeStudentFromCourse(student.id, courseId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
    }

    private fun removeStudentFromCourse(studentId: String, courseId: String) {
        db.collection("enrollments")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { enrollments ->
                val batch = db.batch()
                for (enrollment in enrollments) {
                    batch.delete(enrollment.reference)
                }

                // Update student's courses list
                batch.update(
                    db.collection("users").document(studentId),
                    "courses", FieldValue.arrayRemove(courseId)
                )

                batch.commit()
                    .addOnSuccessListener {
                        loadStudents()
                        Toast.makeText(context, "Student removed from course", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.tvNoStudents)?.visibility =
            if (isEmpty) View.VISIBLE else View.GONE
    }

    class StudentAdapter(
        private val students: List<UserData>,
        private val onItemClick: (UserData) -> Unit
    ) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_student, parent, false)
            return StudentViewHolder(view)
        }

        override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
            holder.bind(students[position])
        }

        override fun getItemCount(): Int = students.size

        inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(student: UserData) {
                itemView.findViewById<TextView>(R.id.tvStudentName).text = student.name
                itemView.findViewById<TextView>(R.id.tvStudentId).text = student.studentId
                itemView.findViewById<TextView>(R.id.tvStudentDepartment).text = student.department

                itemView.setOnClickListener {
                    onItemClick(student)
                }
            }
        }
    }
}