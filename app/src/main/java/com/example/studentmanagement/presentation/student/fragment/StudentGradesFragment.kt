package com.example.studentmanagement.presentation.student.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.data.CourseData
import com.example.studentmanagement.data.EnrollmentData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentGradesFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: GradeAdapter
    private val grades = mutableListOf<Pair<CourseData, EnrollmentData>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_grades, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadStudentGrades()
    }

    private fun setupRecyclerView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.rvGrades)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = GradeAdapter(grades)
        recyclerView?.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadStudentGrades() {
        val studentId = auth.currentUser?.uid ?: return

        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.tvNoGrades)?.visibility = View.GONE

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
                        grades.clear()

                        for (courseDoc in coursesSnapshot) {
                            val course = courseDoc.toObject(CourseData::class.java)
                            for (enrollmentDoc in enrollments) {
                                if (enrollmentDoc.getString("courseId") == course.id) {
                                    val enrollment =
                                        enrollmentDoc.toObject(EnrollmentData::class.java)
                                    grades.add(Pair(course, enrollment))
                                }
                            }
                        }

                        adapter.notifyDataSetChanged()
                        updateEmptyState(grades.isEmpty())
                    }
            }
            .addOnFailureListener {
                updateEmptyState(true)
            }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.tvNoGrades)?.visibility =
            if (isEmpty) View.VISIBLE else View.GONE
    }

    class GradeAdapter(
        private val grades: List<Pair<CourseData, EnrollmentData>>
    ) : RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_grade, parent, false)
            return GradeViewHolder(view)
        }

        override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
            holder.bind(grades[position].first, grades[position].second)
        }

        override fun getItemCount(): Int = grades.size

        inner class GradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(course: CourseData, enrollment: EnrollmentData) {
                itemView.findViewById<TextView>(R.id.tvCourseName).text = course.name
                itemView.findViewById<TextView>(R.id.tvCourseCode).text = course.code
                itemView.findViewById<TextView>(R.id.tvGrade).text =
                    enrollment.grade ?: "Not graded yet"
            }
        }
    }
}