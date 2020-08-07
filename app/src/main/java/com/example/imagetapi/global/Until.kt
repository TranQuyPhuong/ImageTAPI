package com.example.imagetapi.global

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
    val dir = File(context.filesDir, "images")
    if (!dir.exists())
        dir.mkdir()
    return dir
}

fun createDialog(context: Context, content: String, funcOpenSetting: (DialogInterface, Int) -> Unit) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Why provide permission")
    builder.setMessage(content)
    builder.setPositiveButton("Permission Setting", DialogInterface.OnClickListener(function = funcOpenSetting))
    builder.setNegativeButton("Cancel") { dialog, which ->  
        dialog.cancel()
    }
    val dialog = builder.create()
    dialog.show()
}