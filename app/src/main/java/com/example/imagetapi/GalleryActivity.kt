package com.example.imagetapi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.imagetapi.fragment.PhotosFragment
import com.example.imagetapi.viewmodel.ImageViewModel


private const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class GalleryActivity : AppCompatActivity() {

    private lateinit var imageViewModel: ImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        //init view model
        val factory = ImageViewModel(applicationContext)
        imageViewModel = ViewModelProviders.of(this, factory).get(ImageViewModel::class.java)
        val valueObserver = Observer<ArrayList<String>> {
            imageViewModel.getAllImages()
        }

        imageViewModel.getImageList().observe(this, valueObserver)

        if (requestPermission()) {
            val paths = imageViewModel.loadImagesFromSDCard()
            addFragment(paths)
        }

    }

    private fun addFragment(path: ArrayList<String>) {
        val transaction = supportFragmentManager.beginTransaction()
        val photoFragment = PhotosFragment.newInstance(path)
        transaction.add(R.id.frameContainerGallery, photoFragment)
        transaction.commitAllowingStateLoss()
    }

    private fun requestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {

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
            REQUEST_EXTERNAL_STORAGE -> if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                val paths = imageViewModel.loadImagesFromSDCard()
                addFragment(paths)
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}