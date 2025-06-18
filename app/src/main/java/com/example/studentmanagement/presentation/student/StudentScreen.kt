package com.example.studentmanagement.presentation.student

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.presentation.activity.MainActivity
import com.example.studentmanagement.R
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.data.local.PreferencesKeys.ACCOUNT_TYPE
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class StudentScreen : Fragment(R.layout.fragment_student_screen) {

    private val viewModel: StudentViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onAction(StudentAction.LoadStudentData)

        val tvFullName = view.findViewById<TextView>(R.id.tv_full_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)
        val tvAddress = view.findViewById<TextView>(R.id.tv_address)
        val tvPhone = view.findViewById<TextView>(R.id.tv_phone)
        val tvAge = view.findViewById<TextView>(R.id.tv_age)
        val tvStudentId = view.findViewById<TextView>(R.id.tv_student_id)
        val tvGuardian = view.findViewById<TextView>(R.id.tv_guardian)
        val tvGuardianContact = view.findViewById<TextView>(R.id.tv_guardian_contact)
        val tvMajoring = view.findViewById<TextView>(R.id.tv_majoring)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val infoContainer = view.findViewById<LinearLayout>(R.id.student_info_container)
        val btnAskPermission = view.findViewById<View>(R.id.btnAskPermission)
        val btnLogout = view.findViewById<View>(R.id.btnStudentLogout)

        btnLogout.setOnClickListener {
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

        btnAskPermission.setOnClickListener {
            findNavController().navigate(R.id.navigate_student_to_ask_permission)
        }



        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        progressBar.visibility = View.VISIBLE
                        infoContainer.visibility = View.GONE
                    }

                    state.student != null -> {
                        progressBar.visibility = View.GONE
                        infoContainer.visibility = View.VISIBLE

                        val student = state.student
                        tvFullName.text = student.name ?: "N/A"
                        tvEmail.text = student.email ?: "N/A"
                        tvAddress.text = student.address ?: "N/A"
                        tvPhone.text = student.phone ?: "N/A"
                        tvAge.text = student.age?.toString() ?: "N/A"
                        tvStudentId.text = student.studentID ?: "N/A"
                        tvGuardian.text = student.guardian ?: "N/A"
                        tvGuardianContact.text = student.guardianContact ?: "N/A"
                        tvMajoring.text = student.majoring ?: "N/A"
                    }

                    state.error != null -> {
                        progressBar.visibility = View.GONE
                        infoContainer.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${state.error}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }
}
