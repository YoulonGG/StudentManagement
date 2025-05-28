package com.example.studentmanagement.presentation.choose_login_type

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R

class ChooseLoginTypeScreen : Fragment(R.layout.fragment_choose_login_type_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val teacherBtn = view.findViewById<ImageView>(R.id.btnTeacherLogin)
        val studentBtn = view.findViewById<ImageView>(R.id.btnStudentLogin)

        teacherBtn.setOnClickListener {
            findNavController().navigate(
                R.id.navigate_choose_login_type_to_login,
                bundleOf("accountType" to "teacher")
            )
        }

        studentBtn.setOnClickListener {
            findNavController().navigate(
                R.id.navigate_choose_login_type_to_login,
                bundleOf("accountType" to "student")
            )
        }
    }
}