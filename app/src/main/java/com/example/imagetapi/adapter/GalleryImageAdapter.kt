package com.example.imagetapi.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import com.example.imagetapi.R
import kotlinx.android.synthetic.main.item_image_gallery.view.*


class GalleryImageAdapter(private var paths : ArrayList<String>) : RecyclerView.Adapter<GalleryImageAdapter.ImageHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image_gallery, parent, false)
        return ImageHolder(itemView)
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        convertPathToBitmap(paths[position], holder.itemView.imgImageGallery)
    }

    class ImageHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    }

    private fun convertPathToBitmap(path: String, imageView: ImageView) {
        val imgFile = File(path)

        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imageView.setImageBitmap(myBitmap)
        }
    }

}