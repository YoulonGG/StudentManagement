package com.example.studentmanagement.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var accountTypeGroup: RadioGroup

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.signupEmail)
        val pass = findViewById<EditText>(R.id.signupPassword)
        accountTypeGroup = findViewById(R.id.accountTypeGroup)
        val signupBtn = findViewById<Button>(R.id.btnSignup)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)


        val signupTitle = findViewById<TextView>(R.id.signupTitle)
        val accountTypeFromIntent = intent.getStringExtra("accountType") ?: "student"

        signupTitle.text = if (accountTypeFromIntent == "teacher") {
            "Sign Up for Teacher"
        } else {
            "Sign Up for Student"
        }

        signupBtn.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = pass.text.toString().trim()

            errorText.visibility = View.GONE

            if (emailText.isEmpty()) {
                errorText.text = "Email cannot be empty"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!isValidEmail(emailText)) {
                errorText.text = "Invalid email format"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (passwordText.isEmpty()) {
                errorText.text = "Password cannot be empty"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val selectedAccountTypeId = accountTypeGroup.checkedRadioButtonId
            val accountType = when (selectedAccountTypeId) {
                R.id.radioStudent -> "student"
                R.id.radioTeacher -> "teacher"
                else -> "student"
            }

            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val db = FirebaseFirestore.getInstance()

                        val userData = hashMapOf(
                            "email" to emailText,
                            "accountType" to accountType
                        )

                        user?.uid?.let { uid ->
                            db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    startActivity(Intent(this, LoginScreen::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    errorText.text = "Failed to save user data: ${e.message}"
                                    errorText.visibility = View.VISIBLE
                                }
                        }
                    } else {
                        errorText.text = "Signup failed: ${task.exception?.message}"
                        errorText.visibility = View.VISIBLE
                    }
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return email.matches(emailRegex.toRegex())
    }

}


