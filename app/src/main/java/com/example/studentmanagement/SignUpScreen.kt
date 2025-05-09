package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignUpScreen : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView

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
        val signupBtn = findViewById<Button>(R.id.btnSignup)
        progressBar = findViewById(R.id.progressBar) // Initialize ProgressBar
        errorText = findViewById(R.id.errorText) // Initialize Error TextView

        signupBtn.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = pass.text.toString()

            errorText.visibility = View.GONE

            // Validate email
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

            // Validate password
            if (passwordText.isEmpty()) {
                errorText.text = "Password cannot be empty"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Show loading indicator
            progressBar.visibility = View.VISIBLE

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener {
                    progressBar.visibility = View.GONE  // Hide loading indicator

                    if (it.isSuccessful) {
                        startActivity(Intent(this, LoginScreen::class.java))
                        finish()
                    } else {
                        errorText.text = "Signup failed: ${it.exception?.message}"
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

