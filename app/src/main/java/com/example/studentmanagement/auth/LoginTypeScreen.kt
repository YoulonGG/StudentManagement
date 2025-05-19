package com.example.studentmanagement.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.R

class LoginTypeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_type_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val teacherBtn = findViewById<ImageView>(R.id.btnTeacherLogin)
        val studentBtn = findViewById<ImageView>(R.id.btnStudentLogin)

        teacherBtn.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java).apply {
                putExtra("accountType", "teacher")
            })
            finish()
        }

        studentBtn.setOnClickListener {
            startActivity(Intent(this, LoginScreen::class.java).apply {
                putExtra("accountType", "student")
            })
            finish()
        }
    }
}