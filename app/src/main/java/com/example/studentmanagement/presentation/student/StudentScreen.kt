package com.example.studentmanagement.presentation.student

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.studentmanagement.R


class StudentScreen : Fragment(R.layout.fragment_student_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Use this for logging out
//        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//        with(sharedPref.edit()) {
//            clear() // or remove(PreferencesKeys.IS_LOGGED_IN), remove(PreferencesKeys.ACCOUNT_TYPE)
//            apply()
//        }

    }
}
