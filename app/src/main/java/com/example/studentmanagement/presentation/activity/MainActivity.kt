package com.example.studentmanagement.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studentmanagement.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Remove enableEdgeToEdge() to let theme handle status bar
        setContentView(R.layout.activity_main)

        // Remove all the edge-to-edge related code
        // Your theme will now control the status bar color
    }
}