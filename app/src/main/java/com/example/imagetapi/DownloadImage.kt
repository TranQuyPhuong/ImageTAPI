@file:Suppress("DEPRECATION")

package com.example.imagetapi

import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import java.io.*
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
            addImage(bitmap, nameImage)
            //saveImageToInternalStorage(bitmap, nameImage)
            //addImageToGallery(context, bitmap, images[i].id)
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

    // Custom method to save a bitmap into internal storage
    private fun saveImageToInternalStorage(bitmap: Bitmap, id: String): Uri {

        // Initialize ContextWrapper
        val wrapper = ContextWrapper(context.applicationContext)
        // Initializing a new file
        // The bellow line return a directory in internal storage
        val directory: File = wrapper.getDir("ImageTAPI", MODE_PRIVATE)
        // Create a file to save the image
        val file = File(directory, "$id.jpg")
        Log.d("msg", file.absolutePath)

        try {
            // Initialize a new OutputStream
            lateinit var stream: OutputStream

            // If the output file exists, it can be replaced or appended to it
            stream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

            // Flushes the stream
            stream.flush()

            // Closes the stream
            stream.close()
            // display image to gallery
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.path),
                arrayOf("image/jpeg"),
                null
            )
        } catch (e: IOException) // Catch the exception
        {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun addImageToGallery(context: MainActivity, bitmap: Bitmap, photoName: String) {
        val root: String =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/ImageTAPI"
        val myDir = File(root)
        if (!myDir.exists())
            myDir.mkdirs()
        val fileName = "$photoName.png"
        val file = File(myDir, fileName)
        Log.d("msg", file.absolutePath)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            // display image to gallery
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addImage(bitmap: Bitmap, nameImage: String) {
        MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, nameImage, null)
    }

}
