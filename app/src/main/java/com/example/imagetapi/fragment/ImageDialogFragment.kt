package com.example.imagetapi.fragment

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.imagetapi.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.edit_image.*
import kotlinx.android.synthetic.main.item_loading.*
import java.io.File

class ImageDialogFragment : Fragment() {

    private var position = 0
    private lateinit var mListener: ListenerActionDetailImage

    companion object {
        fun newInstance(path: String, position: Int): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val bundle = Bundle()
            bundle.putString("linkPath", path)
            bundle.putInt("position", position)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.MyCustomTheme)
        return inflater.cloneInContext(contextThemeWrapper)
            .inflate(R.layout.edit_image, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imbCancel.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        imbDelete.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
            mListener.removeImage(position)
        }
    }

    override fun onStart() {
        super.onStart()
        val path = arguments?.getString("linkPath")
        position = arguments?.getInt("position")!!

        Picasso.get().load(File(path)).into(editImage, object : Callback {
            override fun onSuccess() {
                itemLoadingMain.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                itemLoadingMain.visibility = View.GONE
            }

        })
    }

    fun createInstanceListener(listener: ListenerActionDetailImage) {
        this.mListener = listener
    }

    interface ListenerActionDetailImage {
        fun removeImage(position: Int)
    }

}