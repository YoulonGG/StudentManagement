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

        try {
            FirebaseApp.initializeApp(this)
            val db = FirebaseFirestore.getInstance()
            Log.e("SplashScreen", "Firebase initialized successfully")

            // Test database connection
            db.collection("test").document("test")
                .get()
                .addOnSuccessListener {
                    Log.e("SplashScreen", "Test connection to Firestore successful")
                }
                .addOnFailureListener { e ->
                    Log.e("SplashScreen", "Test connection to Firestore failed", e)
                }
        } catch (e: Exception) {
            Log.e("SplashScreen", "Firebase initialization failed", e)
        }

        navigateToNextScreen()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, LoginScreen::class.java))
//            finish()
//        }, 2000) // 2000 ms = 2 seconds
    }

    private fun navigateToNextScreen() {
        // Add a delay if you want to show the splash screen for a while
        findViewById<android.view.View>(android.R.id.content).postDelayed({
            // Change MainActivity to wherever you want to navigate
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
            finish()
        }, 2000) // 2-second delay
    }

}
