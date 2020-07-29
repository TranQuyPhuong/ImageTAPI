package com.example.imagetapi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.imagetapi.viewmodel.ImageViewModel

class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        val imageViewModel =
            ViewModelProviders.of(this).get(ImageViewModel::class.java).also {

                it.getImageList().observe(this, Observer<List<String>> { listOfImage ->
                    Log.d("size", """ Found ${listOfImage.size} Images""")
                    // load images
                    it.getAllImages()

                })
            }

    }
}