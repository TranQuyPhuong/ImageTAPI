package com.example.imagetapi.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.R
import com.example.imagetapi.adapter.GalleryImageAdapter
import com.example.imagetapi.datamannage.dataclass.ImageDataClass
import kotlinx.android.synthetic.main.fragment_photos.*

class PhotosFragment : Fragment(),
    ImageDialogFragment.ListenerActionDetailImage, GalleryImageAdapter.ItemImageListener {

    private var paths: ArrayList<ImageDataClass> = ArrayList()
    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var dialog: ImageDialogFragment
    private lateinit var layoutManager: StaggeredGridLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(photos: ArrayList<ImageDataClass>) =
            PhotosFragment().apply {
                val bundle = Bundle()
                bundle.putParcelableArrayList("paths", photos)
                arguments = bundle
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null)
            paths = arguments?.getParcelableArrayList<ImageDataClass>("paths")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(paths)
        setLayoutManage()
    }

    private fun setAdapter(paths: ArrayList<ImageDataClass>) {
        galleryAdapter = GalleryImageAdapter(this, paths)
        recyclerImageGallery.adapter = galleryAdapter
    }

    private fun setLayoutManage() {
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerImageGallery.layoutManager = layoutManager
        recyclerImageGallery.setHasFixedSize(true)
        recyclerImageGallery.adapter = galleryAdapter
    }

    private fun showDialog(path: String, position: Int) {
        dialog = ImageDialogFragment.newInstance(path, position)
        dialog.createInstanceListener(this)
        activity?.supportFragmentManager?.beginTransaction()
            ?.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            ?.add(R.id.frameContainerGallery, dialog)
            ?.addToBackStack("dialog")
            ?.commit()
    }

    private fun deleteItem(position: Int) {
        removeImageIntoGallery(position)
        paths.removeAt(position)
        galleryAdapter.notifyItemRemoved(position)
        galleryAdapter.notifyItemRangeChanged(position, paths.size)
    }

    private fun removeImageIntoGallery(position: Int): Int {
        val resolver = activity?.applicationContext?.contentResolver
        return resolver?.delete(Uri.parse(paths[position].path), null, null)!!
    }

    override fun deleteImage(position: Int) {
        deleteItem(position)
    }

    override fun clickItemImage(position: Int) {
        paths[position].path?.let { showDialog(it, position) }
    }
}