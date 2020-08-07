package com.example.imagetapi.global

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

//view type loading
const val VIEW_TYPE_LOADING = 1
const val VIEW_TYPE_ITEM = 0

// key_access
const val keyAccess = "Dq7t7v4s6jR-5hwHV1r9v8wmhlaY-NIi4zlbriJTH44"

// Connect Internet
fun checkConnectInternet(context: AppCompatActivity): Boolean {
    var result = false
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    else {
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
    }

    return result
}

fun createFolder(context: Context): File {
    val dir = File(context.filesDir, "images")
    if (!dir.exists())
        dir.mkdir()
    return dir
}

fun createDialog(
    context: Context,
    content: String,
    funcOpenSetting: (DialogInterface, Int) -> Unit
) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Why provide permission")
    builder.setMessage(content)
    builder.setPositiveButton(
        "Permission Setting",
        DialogInterface.OnClickListener(function = funcOpenSetting)
    )
    builder.setNegativeButton("Cancel") { dialog, _ ->
        dialog.cancel()
    }
    val dialog = builder.create()
    dialog.show()
}

fun showToastShort(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
}

fun showToastLong(context: Context, content: String) {
    Toast.makeText(context, content, Toast.LENGTH_LONG).show()
}