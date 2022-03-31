package com.yks.chatapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.yks.chatapp.repo.FirebaseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    fun getChats() = firebaseRepo.getChats()

    fun getUserData(uid: String) =
        firebaseRepo.getProfile(uid)

}