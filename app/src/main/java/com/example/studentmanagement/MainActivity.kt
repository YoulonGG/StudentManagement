package com.example.studentmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.main)
        navView = findViewById(R.id.navView)
        topAppBar = findViewById(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginScreen::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }

        findViewById<CardView>(R.id.cardTeacher).setOnClickListener {
            startActivity(Intent(this, TeacherScreen::class.java))
        }
        findViewById<CardView>(R.id.cardStudent).setOnClickListener {
            startActivity(Intent(this, StudentScreen::class.java))
        }
        findViewById<CardView>(R.id.cardCourse).setOnClickListener {
            startActivity(Intent(this, Course::class.java))
        }

        // Optional: set user name/email from Firebase
        val headerView = navView.getHeaderView(0)
        val user = auth.currentUser
        headerView.findViewById<TextView>(R.id.userName).text = user?.displayName ?: "Unknown"
        headerView.findViewById<TextView>(R.id.userEmail).text = user?.email ?: "No Email"
    }
}