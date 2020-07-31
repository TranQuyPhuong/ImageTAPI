package com.example.imagetapi.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetapi.R
import com.example.imagetapi.fragment.PhotosFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_image_gallery.view.*
import java.io.File


class GalleryImageAdapter(private val context: PhotosFragment, private var paths: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_gallery, parent, false)
        return ImageHolder(itemView)
    }

    override fun getItemCount(): Int {
        return paths.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val file = File(paths[position])

        Picasso.get().load(file).resize(1000, 1000).centerCrop().into(holder.itemView.imgImageGallery)

        holder.itemView.setOnClickListener {
            context.editImageDialog(paths, position)
        }

    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

}