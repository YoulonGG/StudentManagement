package com.example.studentmanagement

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.studentmanagement.auth.LoginScreen
import com.example.studentmanagement.student.StudentScreen
import com.example.studentmanagement.teacher.TeacherScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

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
        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
        val accountType = sharedPref.getString("accountType", null)


//        try {
//            FirebaseApp.initializeApp(this)
//            val db = FirebaseFirestore.getInstance()
//            Log.e("SplashScreen", "Firebase initialized successfully")
//
//            // Test database connection
//            db.collection("test").document("test")
//                .get()
//                .addOnSuccessListener {
//                    Log.e("SplashScreen", "Test connection to Firestore successful")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("SplashScreen", "Test connection to Firestore failed", e)
//                }
//        } catch (e: Exception) {
//            Log.e("SplashScreen", "Firebase initialization failed", e)
//        }

        if (isLoggedIn && accountType != null) {
            // User is logged in, redirect based on accountType
            if (accountType == "teacher") {
                startActivity(Intent(this, TeacherScreen::class.java))
            } else {
                startActivity(Intent(this, StudentScreen::class.java))
            }
            finish()
        } else {
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
        }

        navigateToNextScreen()
    }

    private fun navigateToNextScreen() {
        findViewById<android.view.View>(android.R.id.content).postDelayed({
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

}
