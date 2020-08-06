package com.example.imagetapi.viewmodel

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Images
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.imagetapi.datamannage.dataclass.ImageDataClass
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

            val name = cursor.getString(nameColumn)
            linkURIs.add(name)

//            photoUri.toString()?.let { linkURIs.add(it) }
        }
        return linkURIs
    }

    internal fun loadImagesFromStorage(): ArrayList<ImageDataClass> {
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
        val images = ArrayList<ImageDataClass>()

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val photoUri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI, id)

            val name = cursor.getString(nameColumn)

            images.add(ImageDataClass(photoUri.toString(), name))


        }
        return images
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Context::class.java).newInstance(context)
    }
}