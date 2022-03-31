package com.yks.chatapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yks.chatapp.repo.FirebaseRepo
import com.yks.chatapp.utils.Constants.PROFILE_URI_FIELD
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    val downloadUrl: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val downloadUrlException: MutableLiveData<Exception> by lazy {
        MutableLiveData<Exception>()
    }

    fun signOut() =
        firebaseRepo.firebaseAuth.signOut()

    fun updateProfileData(data: HashMap<String, Any>) =
        firebaseRepo.updateProfileData(data)

    fun getProfileImgUri(uid: String? = firebaseRepo.firebaseUser?.uid) =
        firebaseRepo.profileQuery(uid)
            ?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                documentSnapshot?.let {
                    downloadUrl.value = it.getString(PROFILE_URI_FIELD)
                }
            }
            ?.addOnFailureListener { exception ->
                downloadUrlException.value = exception
            }

    fun getProfileData(uid: String? = firebaseRepo.firebaseUser?.uid) =
        firebaseRepo.profileQuery(uid)?.get()

}