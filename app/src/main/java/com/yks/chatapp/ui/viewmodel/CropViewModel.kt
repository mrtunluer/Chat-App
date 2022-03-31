package com.yks.chatapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.yks.chatapp.repo.FirebaseRepo
import com.yks.chatapp.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    fun addProfileImgIntoStorage(uri: Uri) =
        firebaseRepo.addProfileImgIntoStorage(uri)

    fun updateProfileUri(profileUri: String) =
        firebaseRepo.profileQuery()?.update(Constants.PROFILE_URI_FIELD,profileUri)

}