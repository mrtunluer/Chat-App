package com.yks.chatapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.yks.chatapp.repo.FirebaseRepo
import com.yks.chatapp.repo.UserPagingSource
import com.yks.chatapp.utils.Constants.PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    val users = Pager(
        PagingConfig(
            pageSize = PAGE_SIZE
        )
    ) {
        UserPagingSource(firebaseRepo)
    }.flow.cachedIn(viewModelScope)

}