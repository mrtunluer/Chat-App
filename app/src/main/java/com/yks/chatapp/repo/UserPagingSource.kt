package com.yks.chatapp.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.QuerySnapshot
import com.yks.chatapp.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class UserPagingSource(private val firebaseRepo: FirebaseRepo): PagingSource<QuerySnapshot, User>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, User>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, User> {
        return try {
            val currentPage = params.key ?: firebaseRepo.getUsers().get().await()
            val lastVisibleUser = currentPage.documents[currentPage.size() - 1]
            val nextPage = firebaseRepo.getUsers().startAfter(lastVisibleUser).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(User::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}