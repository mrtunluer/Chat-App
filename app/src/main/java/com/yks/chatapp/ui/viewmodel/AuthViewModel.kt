package com.yks.chatapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yks.chatapp.repo.FirebaseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    val isLoggedIn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun addUserIntoDb(uid: String, user: HashMap<String, Any>) =
        firebaseRepo.addUserIntoDb(uid, user)

    fun loginState() {
        isLoggedIn.value = firebaseRepo.firebaseUser != null
    }

    fun signUp(email: String, password: String) =
        firebaseRepo.signUpUser(email, password)

    fun signIn(email: String, password: String) =
        firebaseRepo.signInUser(email, password)

}