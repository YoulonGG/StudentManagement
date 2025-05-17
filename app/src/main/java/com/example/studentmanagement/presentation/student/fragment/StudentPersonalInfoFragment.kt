package com.example.studentmanagement.presentation.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.studentmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class StudentPersonalInfoFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_student_personal_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadStudentInfo()
    }

    private fun loadStudentInfo() {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        view?.findViewById<TextView>(R.id.tvName)?.text =
                            "Name: ${document.getString("name")}"
                        view?.findViewById<TextView>(R.id.tvEmail)?.text =
                            "Email: ${document.getString("email")}"
                        view?.findViewById<TextView>(R.id.tvStudentId)?.text =
                            "Student ID: ${document.getString("studentId")}"
                        view?.findViewById<TextView>(R.id.tvDepartment)?.text =
                            "Department: ${document.getString("department")}"
                    }
                }
        }
    }
}
