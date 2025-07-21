package com.example.studentmanagement.presentation.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.studentmanagement.R
import com.example.studentmanagement.core.utils.animateNav
import com.example.studentmanagement.data.local.PreferencesKeys
import com.example.studentmanagement.databinding.FragmentOnBoardingScreenBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnBoardingScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var onboardingItems: List<OnboardingItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onboardingItems = getOnboardingItems()
        setupViewPager()
        setupClickListeners()
        updateUI(0)
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = onboardingAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUI(position)
            }
        })

        binding.dotsIndicator.attachTo(binding.viewPager)
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }

        binding.btnGetStarted.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun updateUI(position: Int) {
        val isLastPage = position == onboardingAdapter.itemCount - 1

        binding.btnNext.isVisible = !isLastPage
        binding.btnGetStarted.isVisible = isLastPage

        val bgColor =
            ContextCompat.getColor(requireContext(), onboardingItems[position].backgroundColorRes)
        binding.root.setBackgroundColor(bgColor)
    }

    private fun completeOnboarding() {
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(PreferencesKeys.ONBOARDING_COMPLETED, true).apply()
        val isLoggedIn = sharedPref.getBoolean(PreferencesKeys.IS_LOGGED_IN, false)
        val accountType = sharedPref.getString(PreferencesKeys.ACCOUNT_TYPE, null)

        val destination = if (isLoggedIn && accountType != null) {
            if (accountType == "teacher") {
                R.id.navigate_onboarding_to_teacher_screen
            } else {
                R.id.navigate_onboarding_to_student_screen
            }
        } else {
            R.id.navigate_onboarding_to_loginType
        }

        findNavController().navigate(
            destination,
            null,
            animateNav()
        )
    }


    private fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem(
                image = R.drawable.onboarding_img_1,
                title = "Mark Homework\nas completed",
                backgroundColorRes = R.color.onboarding_bg_1
            ),
            OnboardingItem(
                image = R.drawable.onboarding_img_2,
                title = "Rectify your\nAttendance",
                backgroundColorRes = R.color.onboarding_bg_2
            ),
            OnboardingItem(
                image = R.drawable.onboarding_img_3,
                title = "Student Exam\n& Report Cards",
                backgroundColorRes = R.color.onboarding_bg_3
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
