package com.yks.chatapp.repo

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.yks.chatapp.model.Chat
import com.yks.chatapp.model.Conversation
import com.yks.chatapp.model.User
import com.yks.chatapp.utils.Constants.CHAT_IMG_PATH
import com.yks.chatapp.utils.Constants.CONVERSATIONS_COLLECTION
import com.yks.chatapp.utils.Constants.CREATED_AT_FIELD
import com.yks.chatapp.utils.Constants.IMAGE_EXTENSION
import com.yks.chatapp.utils.Constants.MESSAGES_COLLECTION
import com.yks.chatapp.utils.Constants.PAGE_SIZE
import com.yks.chatapp.utils.Constants.PROFILE_IMG_PATH
import com.yks.chatapp.utils.Constants.USERS_COLLECTION
import com.yks.chatapp.utils.Constants.USERS_FIELD
import com.yks.chatapp.utils.State
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.collections.HashMap


class FirebaseRepo @Inject constructor(firebase: Firebase) {

    val firebaseAuth = firebase.auth
    val firebaseUser = firebaseAuth.currentUser
    private val firebaseFirestore = firebase.firestore
    private val storageReference = firebase.storage.reference

    fun getMessages(uid: String): Flow<State<List<Chat>>> = callbackFlow {
        trySend(State.Loading()).isSuccess
        val subscription = messageQuery(uid)?.addSnapshotListener { snapshots, exception ->
            exception?.let {
                trySend(State.Failed(it.message.toString())).isSuccess
                cancel(it.message.toString())
            }
            snapshots?.let { querySnapshot ->
                trySend(State.Success(querySnapshot.toObjects(Chat::class.java))).isSuccess
            }
        }
        awaitClose { subscription?.remove() }
    }

    fun getProfile(uid: String): Flow<State<User>> = callbackFlow {
        trySend(State.Loading()).isSuccess
        val subscription = profileQuery(uid)?.addSnapshotListener { snapshot, exception ->
            exception?.let {
                trySend(State.Failed(it.message.toString())).isSuccess
                cancel(it.message.toString())
            }
            if (snapshot!!.exists())
                trySend(State.Success(snapshot.toObject(User::class.java)!!)).isSuccess
            else{
                trySend(State.Failed("Failed to fetch data")).isSuccess
                cancel("Failed to fetch data")
            }
        }
        awaitClose { subscription?.remove() }
    }

    fun getChats(): Flow<State<List<Conversation>>> = callbackFlow {
        trySend(State.Loading()).isSuccess
        val subscription = conversationQuery()?.addSnapshotListener { snapshots, exception ->
            exception?.let {
                trySend(State.Failed(it.message.toString())).isSuccess
                cancel(it.message.toString())
            }

            snapshots?.let { querySnapshot ->
                trySend(State.Success(querySnapshot.toObjects(Conversation::class.java))).isSuccess
            }
        }
        awaitClose { subscription?.remove() }
    }

    private fun conversationQuery() =
        firebaseUser?.uid?.let {
            firebaseFirestore
                .collection(USERS_COLLECTION)
                .document(firebaseUser.uid)
                .collection(CONVERSATIONS_COLLECTION)
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
        }

    private fun messageQuery(uid: String) =
        firebaseUser?.let { user ->
            firebaseFirestore.collection(MESSAGES_COLLECTION)
                .whereArrayContains(USERS_FIELD, user.uid.plus(uid))
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
        }

    fun addMessage(uuid: String, data: HashMap<String, Any>) =
        firebaseFirestore.collection(MESSAGES_COLLECTION)
            .document(uuid)
            .set(data)

    fun getUsers() =
        firebaseFirestore
            .collection(USERS_COLLECTION)
            .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

    fun updateProfileData(data: HashMap<String, Any>) =
        profileQuery()?.update(data)

    fun profileQuery(uid: String? = firebaseUser?.uid) =
        uid?.let {
            firebaseFirestore.collection(USERS_COLLECTION)
                .document(it)
        }

    fun addProfileImgIntoStorage(uri: Uri) =
        firebaseUser?.uid?.let { uid ->
            storageReference
                .child(PROFILE_IMG_PATH.plus(uid).plus(IMAGE_EXTENSION))
                .putFile(uri)
        }

    fun addChatImgIntoStorage(uri: Uri) =
        storageReference
            .child(CHAT_IMG_PATH.plus(System.currentTimeMillis().toString()).plus(IMAGE_EXTENSION))
            .putFile(uri)

    fun senderConversation(myUid: String, uid: String) =
        firebaseFirestore.collection(USERS_COLLECTION)
            .document(myUid)
            .collection(CONVERSATIONS_COLLECTION)
            .document(uid)

    fun receiverConversation(myUid: String, uid: String) =
        firebaseFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .collection(CONVERSATIONS_COLLECTION)
            .document(myUid)

    fun runBatchForConversation(
        myConversation: DocumentReference
        ,dataToSender: HashMap<String,Any>
        ,receiverConversation: DocumentReference
        ,dataToReceiver: HashMap<String,Any>
    ) = firebaseFirestore.runBatch { batch ->
        batch.set(myConversation, dataToSender)
        batch.set(receiverConversation, dataToReceiver)
    }

    fun addUserIntoDb(uid: String, user: HashMap<String, Any>) =
        firebaseFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(user)

    fun signUpUser(email: String, password: String) =
        firebaseAuth.createUserWithEmailAndPassword(email,password)

    fun signInUser(email: String, password: String) =
        firebaseAuth.signInWithEmailAndPassword(email, password)

}