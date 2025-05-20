package com.example.studentmanagement.presentation.student.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class StudentCoursesFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: CourseAdapter
    private val courses = mutableListOf<CourseData>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_courses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupAddCourseButton()
        loadStudentCourses()
    }

    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvCourses)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = CourseAdapter(courses) { course ->
            showCourseActionsDialog(course)
        }
        recyclerView?.adapter = adapter
    }

    private fun setupAddCourseButton() {
        view?.findViewById<View>(R.id.fabAddCourse)?.setOnClickListener {
            showAddCourseDialog()
        }
    }

    private fun showAddCourseDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_course, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Enroll in Course")
            .setView(dialogView)
            .setPositiveButton("Enroll") { _, _ ->
                val courseCode =
                    dialogView.findViewById<EditText>(R.id.etCourseCode).text.toString()
                if (courseCode.isNotEmpty()) {
                    enrollInCourse(courseCode)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun enrollInCourse(courseCode: String) {
        val studentId = auth.currentUser?.uid ?: return

        db.collection("courses")
            .whereEqualTo("code", courseCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(context, "Course not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val course = querySnapshot.documents[0]
                val courseId = course.id

                db.collection("enrollments")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .addOnSuccessListener { enrollments ->
                        if (!enrollments.isEmpty) {
                            Toast.makeText(
                                context,
                                "Already enrolled in this course",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }

                        // Create new enrollment
                        val enrollment = hashMapOf(
                            "studentId" to studentId,
                            "courseId" to courseId,
                            "enrollmentDate" to FieldValue.serverTimestamp()
                        )

                        db.collection("enrollments")
                            .add(enrollment)
                            .addOnSuccessListener {
                                db.collection("users").document(studentId)
                                    .update("courses", FieldValue.arrayUnion(courseId))
                                    .addOnSuccessListener {
                                        loadStudentCourses()
                                        Toast.makeText(
                                            context,
                                            "Enrolled successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                    }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadStudentCourses() {
        val studentId = auth.currentUser?.uid ?: return

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvNoCourses)?.visibility = View.GONE

        db.collection("enrollments")
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { enrollments ->
                if (enrollments.isEmpty) {
                    updateEmptyState(true)
                    return@addOnSuccessListener
                }

                val courseIds = enrollments.map { it.getString("courseId") ?: "" }
                db.collection("courses")
                    .whereIn("id", courseIds)
                    .get()
                    .addOnSuccessListener { coursesSnapshot ->
                        courses.clear()
                        courses.addAll(coursesSnapshot.toObjects(CourseData::class.java))
                        adapter.notifyDataSetChanged()
                        updateEmptyState(courses.isEmpty())
                    }
            }
            .addOnFailureListener {
                updateEmptyState(true)
            }
    }

    private fun showCourseActionsDialog(course: CourseData) {
        AlertDialog.Builder(requireContext())
            .setTitle(course.name)
            .setMessage("Code: ${course.code}\nInstructor: ${course.instructorName}")
            .setPositiveButton("View Details") { _, _ ->
                // Could navigate to detailed view
            }
            .setNegativeButton("Drop Course") { _, _ ->
                dropCourse(course.id)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun dropCourse(courseId: String) {
        val studentId = auth.currentUser?.uid ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Drop")
            .setMessage("Are you sure you want to drop this course?")
            .setPositiveButton("Drop") { _, _ ->
                db.collection("enrollments")
                    .whereEqualTo("studentId", studentId)
                    .whereEqualTo("courseId", courseId)
                    .get()
                    .addOnSuccessListener { enrollments ->
                        val batch = db.batch()
                        for (enrollment in enrollments) {
                            batch.delete(enrollment.reference)
                        }

                        batch.update(
                            db.collection("users").document(studentId),
                            "courses", FieldValue.arrayRemove(courseId)
                        )

                        batch.commit()
                            .addOnSuccessListener {
                                loadStudentCourses()
                                Toast.makeText(context, "Course dropped", Toast.LENGTH_SHORT).show()
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
        private val courses: List<CourseData>,
        private val onItemClick: (CourseData) -> Unit
    ) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_course, parent, false)
            return CourseViewHolder(view)
        }

        override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
            holder.bind(courses[position])
        }

        override fun getItemCount(): Int = courses.size

        inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(course: CourseData) {
                itemView.findViewById<TextView>(R.id.tvCourseName).text = course.name
                itemView.findViewById<TextView>(R.id.tvInstructor).text = course.instructorName
                itemView.findViewById<TextView>(R.id.tvCourseCode).text = course.code

                itemView.setOnClickListener {
                    onItemClick(course)
                }
            }
        }
    }
}