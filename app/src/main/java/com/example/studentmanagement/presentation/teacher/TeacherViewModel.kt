package com.example.studentmanagement.presentation.teacher

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * @Author: John Youlong.
 * @Date: 5/19/25.
 * @Email: johnyoulong@gmail.com.
 */
class TeacherViewModel(
    activity: AppCompatActivity,
    private val fragments: List<Fragment>
) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]

}