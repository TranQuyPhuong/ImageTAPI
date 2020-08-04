package com.example.imagetapi.global

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import java.io.File

//view type loading
const val VIEW_TYPE_LOADING = 1
const val VIEW_TYPE_ITEM = 0

// key_access
const val keyAccess = "Dq7t7v4s6jR-5hwHV1r9v8wmhlaY-NIi4zlbriJTH44"

// Internet
fun checkConnectInternet(activity: AppCompatActivity): Boolean {
    val connectivityManager =
        activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun createFolder(context: Context): File {
    val dir = File(context.filesDir, "ImageTAPI")
    if (!dir.exists())
        dir.mkdir()
    return dir
}