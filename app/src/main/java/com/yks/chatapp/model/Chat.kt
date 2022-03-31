package com.yks.chatapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable

data class Chat (
    val id: String = "",
    val image_uri: String = "",
    val users: List<String> = listOf(),
    val message: String = "",
    val receiver_id: String = "",
    val sender_id: String = "",
    @ServerTimestamp
    val created_at: Timestamp? = null
): Serializable