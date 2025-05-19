package com.example.studentmanagement.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.LoginTypeScreen
import com.example.studentmanagement.presentation.student.StudentScreen
import com.example.studentmanagement.presentation.teacher.TeacherScreen

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val accountType = sharedPref.getString("accountType", null)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn && accountType != null) {
                if (accountType == "teacher") {
                    startActivity(Intent(this, TeacherScreen::class.java))
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
                } else {
                    startActivity(Intent(this, StudentScreen::class.java))
                    overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
                }
            } else {
                startActivity(Intent(this, LoginTypeScreen::class.java))
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top)
            }
            finish()
        }, 2000)
    }
}


