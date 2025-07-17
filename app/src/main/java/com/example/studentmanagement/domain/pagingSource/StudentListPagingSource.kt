package com.example.studentmanagement.domain.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.studentmanagement.data.dto.StudentResponse
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class StudentListPagingSource(
    private val query: Query
) : PagingSource<DocumentSnapshot, StudentResponse>() {

    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, StudentResponse> {
        return try {
            val currentPage = if (params.key == null) {
                // First page
                query.limit(params.loadSize.toLong()).get().await()
            } else {
                // Subsequent pages
                query.startAfter(params.key!!).limit(params.loadSize.toLong()).get().await()
            }

            val students = currentPage.documents.mapNotNull { document ->
                try {
                    document.toObject(StudentResponse::class.java)?.also { student ->
                        // Optional: Set document ID if needed
                        // student.id = document.id
                    }
                } catch (e: Exception) {
                    null // Skip malformed documents
                }
            }

            // Get the last document for pagination
            val lastVisible = currentPage.documents.lastOrNull()

            LoadResult.Page(
                data = students,
                prevKey = null, // We don't support backward pagination
                nextKey = if (currentPage.documents.isEmpty() || currentPage.documents.size < params.loadSize) {
                    null // No more pages
                } else {
                    lastVisible // Use for next page
                }
            )
        } catch (e: FirebaseFirestoreException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, StudentResponse>): DocumentSnapshot? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey ?: anchorPage?.nextKey
        }
    }
}