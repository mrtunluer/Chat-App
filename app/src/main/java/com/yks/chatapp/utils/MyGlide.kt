package com.yks.chatapp.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yks.chatapp.R

fun ImageView.download(context: Context, uri: String) {

    Glide.with(context)
        .load(uri)
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.profile)
        .into(this)

}
