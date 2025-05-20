package com.example.studentmanagement.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.R
import com.example.studentmanagement.core.StringRes
import com.example.studentmanagement.presentation.student.StudentScreen
import com.example.studentmanagement.presentation.teacher.TeacherScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.loginEmail)
        val pass = findViewById<EditText>(R.id.loginPassword)
        val loginBtn = findViewById<Button>(R.id.btnLogin)
        val signup = findViewById<TextView>(R.id.tvGoSignup)
        val toolbar = findViewById<ImageView>(R.id.goBack)
        val toolbarTitle = findViewById<TextView>(R.id.titleToolbar)
        val selectionAccountType = intent.getStringExtra("accountType") ?: "student"
        val roleTitle = if (selectionAccountType == "teacher") "Teacher" else "Student"
        toolbarTitle.text = "Login as $roleTitle"

        toolbar.setOnClickListener {
            finish()
        }

        loginBtn.setOnClickListener {
            val emailText = email.text.toString()
            val passText = pass.text.toString()

            val selectedAccountType = intent.getStringExtra("accountType")

            auth.signInWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val uid = user.uid
                            val db = FirebaseFirestore.getInstance()
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val accountType = document.getString("accountType")

                                        if (accountType == selectedAccountType) {
                                            val sharedPref =
                                                getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("isLoggedIn", true)
                                                putString("accountType", accountType)
                                                putString("userId", uid)
                                                apply()
                                            }

                                            if (accountType == "teacher") {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        TeacherScreen::class.java
                                                    )
                                                )
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        StudentScreen::class.java
                                                    )
                                                )
                                            }
                                            finish()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Account type mismatch. Please login as $selectedAccountType.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            auth.signOut()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "User data not found.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this,
                                        "Failed to retrieve user data.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, StringRes.LOGIN_FAILED, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signup.setOnClickListener {
            val selectedAccountType = intent.getStringExtra("accountType") ?: "student"
            val intent = Intent(this, SignUpScreen::class.java)
            intent.putExtra("accountType", selectedAccountType)
            startActivity(intent)
        }
    }
}


