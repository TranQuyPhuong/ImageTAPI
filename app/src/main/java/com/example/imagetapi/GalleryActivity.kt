package com.example.imagetapi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.imagetapi.datamannage.dataclass.ImageDataClass
import com.example.imagetapi.fragment.PhotosFragment
import com.example.imagetapi.viewmodel.ImageViewModel

const val REQUEST_RESULT_DATA = "RESULT_DATA"
private const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class GalleryActivity : AppCompatActivity(), PhotosFragment.ListenerDeleteImage {

    private lateinit var imageViewModel: ImageViewModel

    // list image deleted
    private var listImageDelete: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //init view model
        val factory = ImageViewModel(applicationContext)
        imageViewModel = ViewModelProviders.of(this, factory).get(ImageViewModel::class.java)
        val valueObserver = Observer<ArrayList<String>> {
//            imageViewModel.getAllImages()
        }

        imageViewModel.getImageList().observe(this, valueObserver)

        if (requestPermission()) createListImageGalleryFragment()
    }

    private fun addFragment(path: ArrayList<ImageDataClass>) {
        val transaction = supportFragmentManager.beginTransaction()
        val photoFragment = PhotosFragment.newInstance(path)
        photoFragment.createInstanceListener(this)
        transaction.add(R.id.frameContainerGallery, photoFragment)
        transaction.commitAllowingStateLoss()
    }

    private fun requestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)  true
            else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
                false
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> if (grantResults[0] === PackageManager.PERMISSION_GRANTED) createListImageGalleryFragment()

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun createListImageGalleryFragment() {
        val paths = imageViewModel.loadImagesFromStorage()
        addFragment(paths)
    }

    override fun deleteImage(imageDeleted: String) {
        if (listImageDelete == null) {
            listImageDelete = ArrayList()
            listImageDelete?.add(imageDeleted)
        } else listImageDelete?.add(imageDeleted)
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            sendResultData()
            finish()
            //additional code
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private fun sendResultData() {
        val intent = Intent()
        if (listImageDelete != null && listImageDelete?.size != 0)
            intent.putExtra(REQUEST_RESULT_DATA, listImageDelete)
        setResult(Activity.RESULT_OK, intent)
    }

}