package com.example.studentmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.auth.LoginTypeScreen
import com.example.studentmanagement.student.StudentScreen
import com.example.studentmanagement.teacher.TeacherScreen

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val accountType = sharedPref.getString("accountType", null)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn && accountType != null) {
                if (accountType == "teacher") {
                    startActivity(Intent(this, TeacherScreen::class.java))
                } else {
                    startActivity(Intent(this, StudentScreen::class.java))
                }
            } else {
                startActivity(Intent(this, LoginTypeScreen::class.java))
            }
            finish()
        }, 2000)
    }
}


