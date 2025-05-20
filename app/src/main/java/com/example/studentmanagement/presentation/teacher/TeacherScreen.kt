package com.example.studentmanagement.presentation.teacher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.auth.LoginScreen
import com.example.studentmanagement.databinding.ActivityTeacherScreenBinding
import com.example.studentmanagement.presentation.teacher.fragment.TeacherCourseManagementFragment
import com.example.studentmanagement.presentation.teacher.fragment.TeacherPersonalInfoFragment
import com.example.studentmanagement.presentation.teacher.fragment.TeacherStudentsFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class TeacherScreen : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherScreenBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupViewPager()
        setupLogoutButton()
    }

    private fun setupViewPager() {
        val fragments = listOf(
            TeacherPersonalInfoFragment(),
            TeacherStudentsFragment(),
            TeacherCourseManagementFragment()
        )

        val titles = listOf(
            "Personal Info",
            "Students",
            "Courses"
        )

        binding.viewPager.adapter = TeacherViewModel(this, fragments)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()

            val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}

