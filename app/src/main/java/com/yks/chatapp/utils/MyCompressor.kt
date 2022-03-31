package com.yks.chatapp.utils

import android.content.Context
import id.zelory.compressor.Compressor
import java.io.File

suspend fun File.compress(context: Context) =
    Compressor.compress(context, this)
//reduce the image size in the upload process.
