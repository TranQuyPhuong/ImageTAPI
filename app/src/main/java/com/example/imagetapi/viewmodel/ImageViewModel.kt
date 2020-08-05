package com.example.imagetapi.viewmodel

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Images
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext


class ImageViewModel(private val context: Context) : ViewModel(), CoroutineScope,
    ViewModelProvider.Factory {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private var imagesLiveData: MutableLiveData<ArrayList<String>> = MutableLiveData()

    fun getImageList(): MutableLiveData<ArrayList<String>> {
        return imagesLiveData
    }

    /**
     * Getting All Images Path.
     *
     * Required Storage Permission
     *
     * @return ArrayList with images Path
     */
//    @SuppressLint("Recycle")
//    internal fun loadImagesFromSDCard(): ArrayList<String> {
//        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//        val cursor: Cursor?
//        val columnIndexData: Int
//        val listOfAllImages = ArrayList<String>()
//        lateinit var absolutePathOfImage: String
//
//        val projection =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
//            } else {
//                arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME)
//            }
//
//        cursor =
//            context.contentResolver!!.query(uri, projection, null, null, null)
//
//        columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
//
//        while (cursor.moveToNext()) {
//            absolutePathOfImage = cursor.getString(columnIndexData)
//            listOfAllImages.add(absolutePathOfImage)
//        }
//        return listOfAllImages
//    }

    //    fun getAllImages() {
//        launch(Dispatchers.Main) {
//            imagesLiveData.value = withContext(Dispatchers.IO) {
//                loadImagesFromSDCard()
//            }
//        }
//    }

//    fun loadImage() {
//        val path: String =
//            Environment.getExternalStorageDirectory().toString() + "/Pictures"
//        val directory = File(path)
//        val files: Array<File> = directory.listFiles()
//        Log.d("Files", "Size: " + files.size)
//        for (i in files.indices) {
//            Log.d("Files", "FileName:" + files[i].name)
//        }
//    }

    internal fun loadImagesFromSDCard(): ArrayList<String> {
        val externalUri = Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            Images.Media._ID,
            Images.Media.DISPLAY_NAME,
            Images.Media.DATE_TAKEN,
            Images.Media.RELATIVE_PATH
        )

        val selection = Images.Media.RELATIVE_PATH + " like ? "
        val selectionArgs = arrayOf("%${Environment.DIRECTORY_PICTURES}%")
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.run {
                contentResolver.query(
                    externalUri,
                    projection,
                    selection,
                    selectionArgs,
                    Images.Media.DATE_TAKEN + " DESC"
                )
            }
        } else {
            TODO("VERSION.SDK_INT < Q")
        }

        val idColumn = cursor!!.getColumnIndexOrThrow(Images.Media._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(Images.Media.DISPLAY_NAME)
        val linkURIs = ArrayList<String>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val photoUri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id)

            photoUri.toString()?.let { linkURIs.add(it) }
        }
        return linkURIs
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Context::class.java).newInstance(context)
    }
}