package com.example.studentmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.R
import com.example.studentmanagement.student.StudentScreen
import com.example.studentmanagement.teacher.TeacherScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
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
        val goSignup = findViewById<TextView>(R.id.tvGoSignup)

        loginBtn.setOnClickListener {
            auth.signInWithEmailAndPassword(email.text.toString(), pass.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val db = FirebaseFirestore.getInstance()
                            val uid = user.uid

                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.exists()) {
                                        val accountType = document.getString("accountType")
                                        if (accountType == "teacher") {
                                            startActivity(Intent(this, TeacherScreen::class.java))
                                            val sharedPref =
                                                getSharedPreferences("UserPref", MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("isLoggedIn", true)
                                                putString("accountType", accountType)
                                                putString(
                                                    "userId",
                                                    uid
                                                )
                                                apply()
                                            }

                                        } else {
                                            startActivity(Intent(this, StudentScreen::class.java))
                                            val sharedPref =
                                                getSharedPreferences("UserPref", MODE_PRIVATE)
                                            with(sharedPref.edit()) {
                                                putBoolean("isLoggedIn", true)
                                                putString("accountType", accountType)
                                                putString(
                                                    "userId",
                                                    uid
                                                )
                                                apply()
                                            }

                                        }
                                        finish()
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
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

        }
        goSignup.setOnClickListener {
            startActivity(Intent(this, SignUpScreen::class.java))
        }
    }
}