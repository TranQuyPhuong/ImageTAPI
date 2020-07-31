package com.example.imagetapi.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.R
import com.example.imagetapi.adapter.GalleryImageAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.edit_image.view.*
import kotlinx.android.synthetic.main.fragment_photos.*
import kotlinx.android.synthetic.main.item_loading.view.*
import java.io.File


class PhotosFragment : Fragment() {

    var paths: ArrayList<String> = ArrayList()
    lateinit var galleryAdapter: GalleryImageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onStart() {
        super.onStart()
        if (arguments != null)
            paths = arguments?.getStringArrayList("paths")!!
        setAdapter(paths)
        setLayoutManage()
    }

    companion object {
        @JvmStatic
        fun newInstance(photos: ArrayList<String>) =
            PhotosFragment().apply {
                val bundle = Bundle()
                bundle.putStringArrayList("paths", photos)
                arguments = bundle
            }
    }


    private fun setLayoutManage() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerImageGallery.layoutManager = layoutManager
        recyclerImageGallery.setHasFixedSize(true)
        recyclerImageGallery.adapter = galleryAdapter
    }

    private fun setAdapter(paths: ArrayList<String>) {
        galleryAdapter = GalleryImageAdapter(this, paths)
        galleryAdapter.notifyDataSetChanged()
        recyclerImageGallery.adapter = galleryAdapter
    }

    fun editImageDialog(paths: ArrayList<String>, position: Int) {
//        val displayRectangle = Rect()
//        var window = activity!!.window
//        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val builder =
            AlertDialog.Builder(context, R.style.AppTheme)

        val dialogView = this.layoutInflater.inflate(R.layout.edit_image, fragmentPhoto, false)

//        dialogView.minimumWidth = (displayRectangle.width() * 1f).toInt()
//        dialogView.minimumHeight = (displayRectangle.height() * 1f).toInt()
        builder.setView(dialogView)

        val file = File(paths[position])
        Picasso.get().load(file).into(dialogView.editImage, object : Callback {
            override fun onSuccess() {
                dialogView.itemLoadingMain.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                dialogView.itemLoadingMain.visibility = View.GONE
            }

        })

        val editDialog = builder.create()

        dialogView.imbDelete.setOnClickListener {
            paths.removeAt(position)
            galleryAdapter.notifyItemRemoved(position)
            galleryAdapter.notifyItemChanged(position, paths.size)
            editDialog.dismiss()
        }

        dialogView.imbCancel.setOnClickListener {
            editDialog.dismiss()
        }

        editDialog.show()

    }

}