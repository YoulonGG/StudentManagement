package com.example.studentmanagement.student

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class StudentScreen : AppCompatActivity() {

    private lateinit var viewmodel: StudentViewModel
    private lateinit var allStudents: MutableList<Student>
    private lateinit var db: FirebaseFirestore

    private val ADD_STUDENT_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        val searchField = findViewById<EditText>(R.id.searchField)
        val addButton = findViewById<Button>(R.id.btnAddStudent)
        val recyclerView = findViewById<RecyclerView>(R.id.studentRecyclerView)

        allStudents = mutableListOf()
        viewmodel = StudentViewModel(allStudents)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = viewmodel

        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        if (isNetworkAvailable()) {
            fetchStudents()
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show()
        }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        addButton.setOnClickListener {
            val intent = Intent(this, AddStudentScreen::class.java)
            startActivityForResult(intent, ADD_STUDENT_REQUEST_CODE)
        }
    }

    @SuppressLint("ServiceCast")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_STUDENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            fetchStudents()
        }
    }

    private fun filterStudents(query: String) {
        val lowercaseQuery = query.lowercase()

        val filtered = if (query.isEmpty()) {
            allStudents
        } else {
            allStudents.filter { student ->
                student.firstName.lowercase()
                    .contains(lowercaseQuery) || student.lastName.lowercase()
                    .contains(lowercaseQuery) || student.fullName.lowercase()
                    .contains(lowercaseQuery) || student.email.lowercase()
                    .contains(lowercaseQuery) || student.studentId.lowercase()
                    .contains(lowercaseQuery)
            }
        }

        viewmodel.updateList(filtered)
    }

    private fun fetchStudents() {
        db.collection("students").get().addOnSuccessListener { result ->
            Log.d("StudentScreen", "Successfully retrieved ${result.size()} documents")
            allStudents.clear()

            for (doc in result) {
                try {
                    Log.d("StudentScreen", "Processing document ID: ${doc.id}")
                    val student = doc.toObject(Student::class.java)
                    allStudents.add(student)
                    Log.d(
                        "StudentScreen",
                        "Added student: ${student.firstName} ${student.lastName}"
                    )
                } catch (e: Exception) {
                    Log.e("StudentScreen", "Error converting document: ${doc.id}", e)
                }
            }

            viewmodel.updateList(allStudents)

            if (allStudents.isEmpty()) {
                Log.d("StudentScreen", "No students found in the database")
                Toast.makeText(this, "No students found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->

            Log.e("StudentScreen", "Failed to load students", e)
            Log.e("StudentScreen", "Error details: ${e.message}")
            Toast.makeText(this, "Failed to load students: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}