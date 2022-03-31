package com.yks.chatapp.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.yks.chatapp.R

fun Dialog.progressDialog(){
    this.setCancelable(false)
    this.requestWindowFeature(Window.FEATURE_NO_TITLE)
    this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    this.setContentView(R.layout.custom_progress_dialog)
}