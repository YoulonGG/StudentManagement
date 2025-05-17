package com.example.studentmanagement.presentation.student

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.studentmanagement.R
import com.example.studentmanagement.auth.LoginScreen
import com.example.studentmanagement.databinding.ActivityStudentScreenBinding
import com.example.studentmanagement.presentation.student.fragment.StudentCoursesFragment
import com.example.studentmanagement.presentation.student.fragment.StudentGradesFragment
import com.example.studentmanagement.presentation.student.fragment.StudentPersonalInfoFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class StudentScreen : AppCompatActivity() {

    private lateinit var binding: ActivityStudentScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStudentScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        auth = FirebaseAuth.getInstance()

        setupViewPager()
        setupLogoutButton()
    }

    private fun setupViewPager() {
        val fragments = listOf(
            StudentPersonalInfoFragment(),
            StudentCoursesFragment(),
            StudentGradesFragment()
        )

        val titles = listOf(
            "Personal Info",
            "My Courses",
            "Grades"
        )

        binding.viewPager.adapter = StudentPagerAdapter(this, fragments)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginScreen::class.java))
            finish()
        }
    }
}

class StudentPagerAdapter(
    activity: AppCompatActivity,
    private val fragments: List<Fragment>
) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
