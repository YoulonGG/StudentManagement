package com.example.studentmanagement.core.utils

import androidx.navigation.NavOptions
import com.example.studentmanagement.R

/**
 * @Author: John Youlong.
 * @Date: 7/17/25.
 * @Email: johnyoulong@gmail.com.
 */

fun animateNav() = NavOptions.Builder()
    .setEnterAnim(R.anim.slide_in_right)
    .setExitAnim(R.anim.slide_out_left)      //
    .setPopEnterAnim(R.anim.slide_in_left)   // returning screen slides in from left
    .setPopExitAnim(R.anim.slide_out_right)  // current screen slides out to right
    .build()
