package com.yks.chatapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

data class User (
    val uid: String = "",
    val email: String = "",
    val profile_uri: String = "",
    val username: String = "",
    val bio: String = "",
    @ServerTimestamp
    val created_at: Timestamp? = null
): Serializable