@file:Suppress("DEPRECATION")

package com.example.imagetapi

import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


class DownloadImage(
    private val context: MainActivity,
    private val images: ArrayList<ResponsePhoto>
) :
    AsyncTask<ArrayList<URL>, Int, List<Bitmap>>() {

    private lateinit var mProgressDialog: ProgressDialog

    override fun onProgressUpdate(vararg values: Int?) {
        mProgressDialog.progress = values[0]!!
    }

    override fun onPostExecute(result: List<Bitmap>?) {
        mProgressDialog.dismiss()
        context.myAsyncTask.cancel(false)
        // Loop through the bitmap list
        for (i in result!!.indices) {
            val bitmap = result[i]
            val nameImage = images[i].id
            // Save the bitmap to media storage
            saveBitmap(bitmap = bitmap, displayName = "${nameImage}.jpg")

        }
    }

    override fun doInBackground(vararg params: ArrayList<URL>?): List<Bitmap> {
        val param = params[0]
        val count = param?.size
        var connection: HttpURLConnection? = null
        val bitmaps = ArrayList<Bitmap>()
        for (i in 0 until count!!) {
            val currentURL = param[i]
            try {
                // Initialize a new http url connection
                connection = currentURL.openConnection() as HttpURLConnection?

                // Connect the http url connection
                connection?.connect()

                // Get the input stream from http url connection
                val inputStream: InputStream = connection!!.inputStream

                // Initialize a new BufferedInputStream from InputStream
                val bufferedInputStream = BufferedInputStream(inputStream)

                // Convert BufferedInputStream to Bitmap object
                val bmp = BitmapFactory.decodeStream(bufferedInputStream)

                // Add the bitmap to list
                bitmaps.add(bmp)

                // Publish the async task progress
                // Added 1, because index start from 0
                publishProgress(((i + 1) / count.toFloat() * 100).toInt())
                if (isCancelled) {
                    break
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                // Disconnect the http url connection
                connection!!.disconnect()
            }
        }
        return bitmaps
    }

    override fun onCancelled(result: List<Bitmap>?) {
    }

    override fun onCancelled() {
    }

    override fun onPreExecute() {
        initLoadingDialog()
        mProgressDialog.show()
        mProgressDialog.progress = 0
    }

    private fun initLoadingDialog() {
        // Initialize the progress dialog
        mProgressDialog = ProgressDialog(context)
        mProgressDialog.isIndeterminate = true
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        // Progress dialog title
        mProgressDialog.setTitle("AsyncTask")
        // Progress dialog message
        mProgressDialog.setMessage("Please wait, we are downloading your image files...")
        mProgressDialog.setCancelable(true)
    }

    @Throws(IOException::class)
    private fun saveBitmap(contextParam: Context = context, bitmap: Bitmap,
                           format: CompressFormat = CompressFormat.JPEG,
                           mimeType: String = "image/jpeg",
                           displayName: String) {
        val relativeLocation = Environment.DIRECTORY_PICTURES
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
        }
        val resolver: ContentResolver = contextParam.contentResolver
        var stream: OutputStream? = null
        var uri: Uri? = null
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)
            if (uri == null) {
                throw IOException("Failed to create new MediaStore record.")
            }
            stream = resolver.openOutputStream(uri)
            if (stream == null) {
                throw IOException("Failed to get output stream.")
            }
            if (!bitmap.compress(format, 90, stream)) {
                throw IOException("Failed to save bitmap.")
            }
        } catch (e: IOException) {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null)
            }
            throw e
        } finally {
            stream?.close()
        }
    }

}
