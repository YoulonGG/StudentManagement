package com.example.studentmanagement.presentation.student_list

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.studentmanagement.core.base.BaseViewModel
import com.example.studentmanagement.data.dto.StudentResponse
import com.example.studentmanagement.domain.pagingSource.StudentListPagingSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

class StudentListViewModel(
    private val firestore: FirebaseFirestore
) : BaseViewModel<StudentListAction, StudentListUiState>() {

    private val _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagingDataFlow = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            getStudentsPagingData(query)
        }
        .cachedIn(viewModelScope)

    override fun setInitialState(): StudentListUiState = StudentListUiState()

    override fun onAction(event: StudentListAction) {
        when (event) {
            StudentListAction.StudentList -> {
                setState { copy(isLoading = false, error = null) }
            }

            is StudentListAction.SearchStudents -> {
                _searchQuery.value = event.query
            }
        }
    }

    private fun getStudentsPagingData(query: String): Flow<PagingData<StudentResponse>> {
        val baseQuery = firestore.collection("students")
            .orderBy("name", Query.Direction.ASCENDING)

        val filteredQuery = if (query.isBlank()) {
            baseQuery
        } else {
            baseQuery
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThan("name", query + "\uf8ff")
        }

        return Pager(
            config = PagingConfig(
                pageSize = 5,
                initialLoadSize = 5,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = { StudentListPagingSource(filteredQuery) }
        ).flow
    }
}