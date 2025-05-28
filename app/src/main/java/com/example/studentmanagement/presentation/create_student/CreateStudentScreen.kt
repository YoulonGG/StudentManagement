package com.example.studentmanagement.presentation.create_student

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.studentmanagement.R
import com.example.studentmanagement.data.dto.request.CreateStudentDataRequest

class CreateStudentScreen : AppCompatActivity() {

    private lateinit var viewModel: CreateStudentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[CreateStudentViewModel::class.java]

        val fullName = findViewById<EditText>(R.id.editTextFullName)
        val email = findViewById<EditText>(R.id.editTextEmail)
        val address = findViewById<EditText>(R.id.editTextAddress)
        val phone = findViewById<EditText>(R.id.editTextPhone)
        val age = findViewById<EditText>(R.id.editTextAge)
        val studentId = findViewById<EditText>(R.id.editTextStudentId)
        val guardian = findViewById<EditText>(R.id.editTextGuardian)
        val guardianContact = findViewById<EditText>(R.id.editTextGuardianContact)
        val majoring = findViewById<EditText>(R.id.editTextMajoring)
        val buttonSubmit = findViewById<Button>(R.id.buttonSubmitStudent)

        buttonSubmit.setOnClickListener {
            val intent = CreateStudentAction.OnCreateStudent(
                data = CreateStudentDataRequest(
                    fullName = fullName.text.toString(),
                    email = email.text.toString(),
                    address = address.text.toString(),
                    phone = phone.text.toString(),
                    age = age.text.toString().toInt(),
                    studentId = studentId.text.toString(),
                    guardian = guardian.text.toString(),
                    guardianContact = guardianContact.text.toString(),
                    majoring = majoring.text.toString()
                )
            )
            viewModel.handleIntent(intent)
        }

        viewModel.state.observe(this) { state ->
            if (state.isLoading) {
                Toast.makeText(this, "Creating student...", Toast.LENGTH_SHORT).show()
            } else if (state.isSuccess) {
                Toast.makeText(this, "Student created successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else if (state.errorMessage != null) {
                Toast.makeText(this, "Error: ${state.errorMessage}", Toast.LENGTH_LONG).show()
            }
        }

    }
}