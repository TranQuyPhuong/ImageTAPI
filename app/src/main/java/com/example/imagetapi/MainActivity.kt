@file:Suppress("DEPRECATION")

package com.example.imagetapi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.adapter.ImageAdapter
import com.example.imagetapi.datamannage.APIClient
import com.example.imagetapi.datamannage.APIInterface
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import com.example.imagetapi.global.checkConnectInternet
import com.example.imagetapi.viewmodel.ImageViewModel
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.MalformedURLException
import java.net.URL

private const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

@Suppress("DEPRECATED_IDENTITY_EQUALS", "UNREACHABLE_CODE")
class MainActivity : AppCompatActivity() {

    private val apiService = APIClient.client.create(APIInterface::class.java)
    private lateinit var imageAdapter: ImageAdapter
    lateinit var photos: ArrayList<ResponsePhoto?>

    lateinit var myAsyncTask: DownloadImage

    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: RecyclerViewLoadMoreScroll

    //instance ImageViewModel
    private lateinit var imageViewModel: ImageViewModel

    //list name image download
    private var nameImages: ArrayList<String>? = null

    //number page default = 1
    var newPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init view model
        val factory = ImageViewModel(applicationContext)
        imageViewModel =
            ViewModelProviders.of(this, factory).get(ImageViewModel::class.java)

        if (requestPermission()) {
            imgDownload.visibility = View.VISIBLE
            this.nameImages = getNameImageListDownload()
            if (!checkConnectInternet(this))
                Toast.makeText(this, "Internet is not Available", Toast.LENGTH_SHORT).show()
            else
                loadPhotos()
        }

        //listener click download button
        imgDownload.setOnClickListener {
            // init Async Task
            val data = getChooseImages()
            if (data.size != 0) {
                myAsyncTask = DownloadImage(this, data)
                downloadImage(data)
            } else
                Toast.makeText(this, "Choose new image to download", Toast.LENGTH_SHORT).show()
        }
        //listener click go to gallery
        imgGoToGallery.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loadPhotos() {
        val call = apiService.getPhotos()
        call.enqueue(object : Callback<ArrayList<ResponsePhoto?>> {
            override fun onFailure(call: Call<ArrayList<ResponsePhoto?>>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<ArrayList<ResponsePhoto?>>,
                response: Response<ArrayList<ResponsePhoto?>>
            ) {
                photos = response.body()!!
                compareImageDownloaded(photos)
                initComponentView()
                newPage += 1
            }

        })
    }

    private fun compareImageDownloaded(images: ArrayList<ResponsePhoto?>) {
        if (nameImages != null) {
            for (j in 0 until nameImages!!.size) {
                for (i in 0 until images.size) {
                    if (images[i]?.id?.compareTo(nameImages!![j].replace(".jpg", "")) == 0) {
                        images[i]?.isDownload = true
                        break
                    }
                }
            }
        }
    }

    private fun getNameImageListDownload(): ArrayList<String>? {
        var nameImages: ArrayList<String>? = null
        val pathImageList = getPathImageListDownload()
        if (pathImageList != null) {
            nameImages = ArrayList()
            for (i in 0 until pathImageList.size) {
                val file = File(pathImageList[i])
                nameImages.add(file.name)
            }
        }
        return nameImages
    }

    private fun getPathImageListDownload(): ArrayList<String>? {
        return imageViewModel.loadImagesFromSDCard()
    }

    private fun initComponentView() {
        setAdapter()
        setLayoutManager()
        setRVScrollListener()
    }

    private fun getChooseImages(): ArrayList<ResponsePhoto> {
        // list image choose to download
        val param = imageAdapter.getChooseImages()
        val imageDownloaded = getNameImageListDownload()
        if (imageDownloaded != null || imageDownloaded!!.size != 0) {
            for (i in 0 until imageDownloaded!!.size) {
                for (j in 0 until param.size) {
                    if (param[j].id.compareTo(imageDownloaded!![i].replace(".jpg", "")) == 0) {
                        param.removeAt(j)
                        break
                    }
                }
            }

        }

        return param
    }

    private fun downloadImage(data: ArrayList<ResponsePhoto>) {
        val params = ArrayList<URL>()
        for (i in 0 until data.size) {
            val url: URL = stringToURL(data[i].url.small)!!
            params.add(url)
        }
        myAsyncTask.execute(params)
    }

    // Custom method to convert string to url
    private fun stringToURL(urlString: String?): URL? {
        try {
            return URL(urlString)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setAdapter() {
        imageAdapter = ImageAdapter(photos)
        imageAdapter.notifyDataSetChanged()
        recyclerImage.adapter = imageAdapter
    }

    private fun setLayoutManager() {
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        (mLayoutManager as StaggeredGridLayoutManager).gapStrategy =
            StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        recyclerImage.layoutManager = mLayoutManager
        recyclerImage.setHasFixedSize(true)
        recyclerImage.adapter = imageAdapter
    }

    private fun setRVScrollListener() {
        scrollListener = RecyclerViewLoadMoreScroll(mLayoutManager as StaggeredGridLayoutManager)
        scrollListener.setOnLoadMoreListener(object :
            OnLoadMoreListener {
            override fun onLoadMore() {
                loadMorePhoto()
            }
        })

        recyclerImage.addOnScrollListener(scrollListener)
    }

    fun loadMorePhoto() {
        val call = apiService.getPhotos(page = newPage)
        call.enqueue(object : Callback<ArrayList<ResponsePhoto?>> {
            override fun onFailure(call: Call<ArrayList<ResponsePhoto?>>, t: Throwable) {
            }

            override fun onResponse(
                call: Call<ArrayList<ResponsePhoto?>>,
                response: Response<ArrayList<ResponsePhoto?>>
            ) {
                response.body()?.let {
                    compareImageDownloaded(it)
                    loadMoreData(it)
                }

            }

        })
    }

    fun loadMoreData(photos: ArrayList<ResponsePhoto?>) {
        //Add the Loading View
        imageAdapter.addLoadingView()
        //use handler to see progressbar show (because too fast)
        Handler().postDelayed({
            //Remove the Loading View
            imageAdapter.removeLoadingView()
            //We adding the data to our main ArrayList
            imageAdapter.addData(photos)
            //Change the boolean isLoading to false
            scrollListener.setLoaded()
            //Update the recyclerView in the main thread
            recyclerImage.post {
                imageAdapter.notifyDataSetChanged()
            }
            newPage += 1
        }, 1000)

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

                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
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
            REQUEST_EXTERNAL_STORAGE -> {
                if (grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    imgDownload.visibility = View.VISIBLE
                    this.nameImages = getNameImageListDownload()
                    if (!checkConnectInternet(this))
                        Toast.makeText(this, "Internet is not Available", Toast.LENGTH_SHORT).show()
                    else
                        loadPhotos()

                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}