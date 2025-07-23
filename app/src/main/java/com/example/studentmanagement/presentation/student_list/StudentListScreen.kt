package com.example.studentmanagement.presentation.student_list

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R
import com.example.studentmanagement.core.ui_components.Dialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StudentListFragment : Fragment(R.layout.fragment_student_list_screen) {

    private val viewModel: StudentListViewModel by viewModel()
    private val adapter = StudentPagingAdapter()
    private val loadingStateAdapter = SimpleLoadingStateAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_students)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val backBtn = view.findViewById<ImageView>(R.id.goBack)
        val searchInput = view.findViewById<EditText>(R.id.search_input)
        val clearIcon = view.findViewById<ImageView>(R.id.clear_icon)
        val studentListTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        studentListTitle.text = getString(R.string.student_list)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = adapter.withLoadStateFooter(
            footer = loadingStateAdapter
        )

        viewModel.onAction(StudentListAction.StudentList)

        lifecycleScope.launch {
            viewModel.pagingDataFlow.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.error?.let {
                    Dialog.showDialog(
                        requireContext(),
                        title = "Error",
                        description = it,
                        onBtnClick = {}
                    )
                }
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                progressBar.visibility = when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                    }

                    else -> View.GONE
                }

                val errorState = loadStates.source.append as? LoadState.Error
                    ?: loadStates.source.prepend as? LoadState.Error
                    ?: loadStates.append as? LoadState.Error
                    ?: loadStates.prepend as? LoadState.Error
                    ?: loadStates.refresh as? LoadState.Error

                errorState?.let {
                    Dialog.showDialog(
                        requireContext(),
                        title = "Error",
                        description = it.error.message ?: "An error occurred",
                        onBtnClick = {}
                    )
                }
            }
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearIcon.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                viewModel.onAction(StudentListAction.SearchStudents(s.toString()))
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearIcon.setOnClickListener {
            searchInput.text.clear()
            viewModel.onAction(StudentListAction.SearchStudents(""))
            hideKeyboard()
        }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}