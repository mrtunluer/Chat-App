package com.yks.chatapp.utils

import android.Manifest

object Constants {

    const val PROFILE_URI_FIELD = "profile_uri"
    const val BIO_FIELD = "bio"
    const val CREATED_AT_FIELD = "created_at"
    const val MESSAGE_FIELD = "message"
    const val RECEIVER_ID_FIELD = "receiver_id"
    const val SENDER_ID_FIELD = "sender_id"
    const val ID_FIELD = "id"
    const val EMAIL_FIELD = "email"
    const val IMAGE_URI_FIELD = "image_uri"
    const val USER_NAME_FIELD = "username"
    const val USERS_FIELD = "users"
    const val UID_FIELD = "uid"

    const val USERS_COLLECTION = "Users"
    const val MESSAGES_COLLECTION = "Messages"
    const val CONVERSATIONS_COLLECTION = "Conversations"

    const val NUM_TABS = 2
    val TABS_FRAGMENT_TEXT = arrayOf("Users","Chats")

    const val SENT_VIEW_TYPE = 0
    const val RECEIVED_VIEW_TYPE = 1
    const val SENT_IMG_VIEW_TYPE = 2
    const val RECEIVED_IMG_VIEW_TYPE = 3

    const val PAGE_SIZE = 20

    const val MIN_SCALE = 0.85f
    const val MIN_ALPHA = 0.5f

    const val READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE

    const val OPEN_GALLERY = "image/*"
    const val PROFILE_IMG_PATH = "profile/"
    const val CHAT_IMG_PATH = "chat/"
    const val IMAGE_EXTENSION = ".jpg"

    const val SPAN_COUNT = 4

    const val DATE_PATTERN = "dd-MM-yy"


}