package com.example.imagetapi.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.R
import com.example.imagetapi.adapter.GalleryImageAdapter
import kotlinx.android.synthetic.main.fragment_photos.*

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
        setAdapter()
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

    private fun setAdapter() {
        galleryAdapter = GalleryImageAdapter(paths)
        galleryAdapter.notifyDataSetChanged()
        recyclerImageGallery.adapter = galleryAdapter
    }

}