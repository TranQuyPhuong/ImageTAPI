package com.example.imagetapi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.R
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import com.example.imagetapi.global.VIEW_TYPE_ITEM
import com.example.imagetapi.global.VIEW_TYPE_LOADING
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_image.view.*

class ImageAdapter(private var photos: ArrayList<ResponsePhoto?>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    var itemStateArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == VIEW_TYPE_ITEM) {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            ImageViewHolder(itemView)
        } else {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(itemView)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (photos[position] == null) VIEW_TYPE_LOADING
        else VIEW_TYPE_ITEM

    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_ITEM) {
            Picasso.get().load(photos[position]?.url?.small)
                .into(holder.itemView.itemImage, object : Callback {
                    override fun onSuccess() {
                        holder.itemView.itemLoading.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        holder.itemView.itemLoading.visibility = View.GONE
                    }
                })
            holder.itemView.chkDownloadImage.isChecked = photos[position]!!.isDownload
            holder.itemView.chkDownloadImage.setOnCheckedChangeListener { _, isChecked ->
                photos[holder.adapterPosition]?.isDownload = isChecked
            }
//            holder.itemView.chkDownloadImage.isChecked = itemStateArray.get(position, false)
//            holder.itemView.chkDownloadImage.setOnCheckedChangeListener { _, isChecked ->
//                itemStateArray.put(holder.adapterPosition, isChecked)
//            }

        } else {
            val layoutParams =
                holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = true
        }
    }

    fun addData(addData: ArrayList<ResponsePhoto?>) {
        this.photos.addAll(addData)
        notifyDataSetChanged()
    }

    fun addLoadingView() {
        //add loading item
        photos.add(null)
        notifyItemInserted(photos.size - 1)
    }

    fun removeLoadingView() {
        //remove loading item
        if (photos.size != 0) {
            photos.removeAt(photos.size - 1)
            notifyItemRemoved(photos.size)
        }
    }

    fun getChooseImages(): ArrayList<ResponsePhoto> {
        val images = ArrayList<ResponsePhoto>()
        for (i in 0 until photos.size) {
            if (photos[i]!!.isDownload)
                photos[i]?.let { images.add(it) }
        }
        return images
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}