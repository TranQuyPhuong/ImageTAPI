@file:Suppress("DEPRECATION")

package com.example.imagetapi

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.provider.MediaStore.Images
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DownloadImage(private val context: MainActivity) :
    AsyncTask<ArrayList<URL>, Int, List<Bitmap>>() {

    private lateinit var mProgressDialog: ProgressDialog

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        mProgressDialog.progress = values[0]!!
    }

    override fun onPostExecute(result: List<Bitmap>?) {
        super.onPostExecute(result)
        // Hide the progress dialog

        // Hide the progress dialog
        mProgressDialog.dismiss()

        // Loop through the bitmap list
        for (i in result!!.indices) {
            val bitmap = result[i]
            // Save the bitmap to internal storage
            val imageInternalUri: Uri = saveImageToInternalStorage(bitmap, i)
            // Display bitmap from internal storage
            addImageToGallery(imageInternalUri)
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
        super.onCancelled(result)
    }

    override fun onCancelled() {
        super.onCancelled()
    }

    override fun onPreExecute() {
        initLoadingDialog()
        mProgressDialog.progress = 0
        mProgressDialog.show()
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

    // Custom method to save a bitmap into internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap, index: Int): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Image TAPI",
            null
        )
        return Uri.parse(path)
    }

    // Custom method to add a new image view using uri
    private fun addImageToGallery(uri: Uri?) {
        addImageToGallery(uri?.path, context.applicationContext)
    }

    @SuppressLint("InlinedApi")
    fun addImageToGallery(filePath: String?, context: Context) {
        val values = ContentValues()
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.DATA, filePath)
        context.contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
    }

}