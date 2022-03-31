package com.yks.chatapp.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.yks.chatapp.repo.FirebaseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepo
) : ViewModel(){

    fun receiverConversation(myUid: String, uid: String) =
        firebaseRepo.receiverConversation(myUid, uid)

    fun senderConversation(myUid: String, uid: String) =
        firebaseRepo.senderConversation(myUid, uid)

    fun runBatchForConversation(
        myConversation: DocumentReference
        ,dataToSender: HashMap<String,Any>
        ,receiverConversation: DocumentReference
        ,dataToReceiver: HashMap<String,Any>
    ) = firebaseRepo.runBatchForConversation(
        myConversation
        ,dataToSender
        ,receiverConversation
        ,dataToReceiver)

    fun addChatImgIntoStorage(uri: Uri) = firebaseRepo.addChatImgIntoStorage(uri)

    fun getMessages(uid: String) = firebaseRepo.getMessages(uid)

    fun getProfile(uid: String) = firebaseRepo.getProfile(uid)

    fun addMessage(uuid: String, data: HashMap<String, Any>) = firebaseRepo.addMessage(uuid, data)

    val mUid = firebaseRepo.firebaseUser?.uid

}