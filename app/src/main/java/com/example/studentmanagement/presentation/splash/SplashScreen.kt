package com.example.studentmanagement.presentation.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studentmanagement.R
import com.example.studentmanagement.core.utils.animateNav
import com.example.studentmanagement.data.local.PreferencesKeys

@SuppressLint("CustomSplashScreen")
class SplashFragment : Fragment(R.layout.activity_splash_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean(PreferencesKeys.IS_LOGGED_IN, false)
        val accountType = sharedPref.getString(PreferencesKeys.ACCOUNT_TYPE, null)
        val isOnboardingCompleted = sharedPref.getBoolean(PreferencesKeys.ONBOARDING_COMPLETED, false)

        Handler(Looper.getMainLooper()).postDelayed({
            val navController = findNavController()

            if (!isOnboardingCompleted) {
                navController.navigate(
                    R.id.navigate_splash_to_onboarding,
                    null,
                    animateNav()
                )
                return@postDelayed
            }

            if (isLoggedIn && accountType != null) {
                if (accountType == "teacher") {
                    navController.navigate(
                        R.id.navigate_splash_to_teacher_screen,
                        null,
                        animateNav()
                    )
                } else {
                    navController.navigate(
                        R.id.navigate_splash_to_student_screen,
                        null,
                        animateNav()
                    )
                }
            } else {
                navController.navigate(
                    R.id.navigate_splash_to_loginType,
                    null,
                    animateNav()
                )
            }
        }, 2500)
    }
}

