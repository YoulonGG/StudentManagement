package com.example.studentmanagement.presentation.student_list

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class StudentListFragment : Fragment(R.layout.fragment_student_list_screen) {

    private val viewModel: StudentListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_students)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = viewModel.adapter

        viewModel.onAction(StudentListAction.StudentList)

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                recyclerView.visibility =
                    if (!state.isLoading && state.student.isNotEmpty()) View.VISIBLE else View.GONE

                state.error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
