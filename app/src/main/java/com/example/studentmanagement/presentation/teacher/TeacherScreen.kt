package com.example.studentmanagement.presentation.teacher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.presentation.activity.MainActivity
import com.example.studentmanagement.R
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.data.local.PreferencesKeys.ACCOUNT_TYPE


class TeacherScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teacher_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seeStudentListBtn = view.findViewById<Button>(R.id.btnSeeStudentList)
        val createStudentBtn = view.findViewById<Button>(R.id.btnCreateStudent)
        val logoutBtn = view.findViewById<Button>(R.id.btnLogout)
        val btnAttendance = view.findViewById<Button>(R.id.btnAttendance)
        val btnAttendanceHistory = view.findViewById<Button>(R.id.btnAttendanceHistory)
        val btnHomeWork = view.findViewById<Button>(R.id.btnHomework)

        btnHomeWork.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val accountType = sharedPref.getString(ACCOUNT_TYPE, "teacher") ?: ""

            val bundle = Bundle().apply {
                putString("accountType", accountType)
            }
            findNavController().navigate(R.id.navigate_teacher_to_homework, bundle)
        }

        btnAttendanceHistory.setOnClickListener {
            findNavController().navigate(R.id.navigate_teacher_to_attendance_history)
        }

        btnAttendance.setOnClickListener {
            findNavController().navigate(R.id.navigate_teacher_to_attendance)
        }

        seeStudentListBtn.setOnClickListener {
            findNavController().navigate(R.id.navigate_teacher_to_student_list)
        }

        createStudentBtn.setOnClickListener {
            findNavController().navigate(R.id.navigate_teacher_to_approve_student)
        }

        logoutBtn.setOnClickListener {
            val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                remove(PreferencesKeys.IS_LOGGED_IN)
                remove(ACCOUNT_TYPE)
                apply()
            }

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
