package com.example.studentmanagement.student

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

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

        val teacherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, teacherList)
        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teacherSpinner.adapter = teacherAdapter

        val dateOfBirthInput = findViewById<EditText>(R.id.dateOfBirth)
        if (dateOfBirthInput == null) {
            Log.e("AddStudentScreen", "dateOfBirth view is null! Check your layout.")
        }

        dateOfBirthInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay.toString().padStart(2, '0')}/" +
                        "${(selectedMonth + 1).toString().padStart(2, '0')}/" +
                        "$selectedYear"
                dateOfBirthInput.setText(selectedDate)
            }, year, month, day)

            datePicker.show()
        }

        db.collection("teachers").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val teacherName = document.getString("name")
                    teacherName?.let { teacherList.add(it) }
                }
                teacherAdapter.notifyDataSetChanged()

                if (teacherList.isNotEmpty()) {
                    teacherSpinner.setSelection(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AddStudentScreen", "Error loading teachers", exception)
                Toast.makeText(
                    this,
                    "Failed to load teachers: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        val courseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseList)
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = courseAdapter

        db.collection("courses").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val courseName = document.getString("name")
                    courseName?.let { courseList.add(it) }
                }
                courseAdapter.notifyDataSetChanged()

                if (courseList.isNotEmpty()) {
                    courseSpinner.setSelection(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AddStudentScreen", "Error loading courses", exception)
                Toast.makeText(
                    this,
                    "Failed to load courses: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val studentId = studentIdInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            // Basic validation
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || studentId.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val student = Student(firstName, lastName, email, studentId, phone)

            saveButton.isEnabled = false
            saveButton.text = "Saving..."

            db.collection("students").add(student)
                .addOnSuccessListener {
                    Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("AddStudentScreen", "Error adding student", e)
                    Toast.makeText(this, "Failed to add student: ${e.message}", Toast.LENGTH_SHORT)
                        .show()

                    // Re-enable button
                    saveButton.isEnabled = true
                    saveButton.text = "Save"
                }
        }
    }
}
