package com.example.studentmanagement

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddStudentScreen : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student_screen)

        db = FirebaseFirestore.getInstance()

        val teacherSpinner = findViewById<Spinner>(R.id.teacherSpinner)
        val courseSpinner = findViewById<Spinner>(R.id.courseSpinner)
        val firstNameInput = findViewById<EditText>(R.id.firstName)
        val lastNameInput = findViewById<EditText>(R.id.lastName)
        val emailInput = findViewById<EditText>(R.id.email)
        val studentIdInput = findViewById<EditText>(R.id.studentId)
        val phoneInput = findViewById<EditText>(R.id.phoneNumber)
        val saveButton = findViewById<Button>(R.id.saveButton)

        val teacherList = mutableListOf<String>()
        val courseList = mutableListOf<String>()

        // Set up teacher spinner
        val teacherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teacherList)
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teacherSpinner.adapter = teacherAdapter

        // Fetch teacher data from Firebase
        db.collection("teachers").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val teacherName = document.getString("name")
                    teacherName?.let { teacherList.add(it) }
                }
                teacherAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load teachers", Toast.LENGTH_SHORT).show()
            }

        // Set up course spinner
        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseList)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        // Fetch course data from Firebase
        db.collection("courses").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val courseName = document.getString("name")
                    courseName?.let { courseList.add(it) }
                }
                courseAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show()
            }

        // Save button click listener
        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val email = emailInput.text.toString()
            val studentId = studentIdInput.text.toString()
            val phone = phoneInput.text.toString()

            // Create student object (add validation before this)
            val student = Student(firstName, lastName, email, studentId, phone)

            // Add student to Firestore
            db.collection("students").add(student)
                .addOnSuccessListener {
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                    finish()  // Close the current screen and go back
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
