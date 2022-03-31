package com.yks.chatapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

data class Conversation (
    val id: String = "",
    val image_uri: String = "",
    val message: String = "",
    @ServerTimestamp
    val created_at: Timestamp? = null
): Serializable