package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class StudentScreen : AppCompatActivity() {

    private lateinit var adapter: StudentViewModel
    private lateinit var allStudents: MutableList<Student>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchField = findViewById<EditText>(R.id.searchField)
        val addButton = findViewById<Button>(R.id.btnAddStudent)
        val recyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)

        allStudents = mutableListOf()
        adapter = StudentViewModel(allStudents)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchStudents()

        searchField.addTextChangedListener {
            val query = it.toString().lowercase()
            val filtered = allStudents.filter {
                it.fullName.lowercase().contains(query)
            }
            adapter.updateList(filtered)
        }

        addButton.setOnClickListener {
            startActivity(Intent(this, AddStudentScreen::class.java))
        }

    }

    private fun fetchStudents() {
        FirebaseFirestore.getInstance().collection("students")
            .get()
            .addOnSuccessListener { result ->
                allStudents.clear()
                for (doc in result) {
                    val student = doc.toObject(Student::class.java)
                    allStudents.add(student)
                }
                adapter.updateList(allStudents)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
    }

}