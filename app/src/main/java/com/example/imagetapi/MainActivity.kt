package com.example.imagetapi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.adapter.ImageAdapter
import com.example.imagetapi.datamannage.APIClient
import com.example.imagetapi.datamannage.APIInterface
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import com.example.imagetapi.global.checkConnectInternet
import com.example.imagetapi.global.createDialog
import com.example.imagetapi.viewmodel.ImageViewModel
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.MalformedURLException
import java.net.URL

private const val REQUEST_RESULT = 111
private const val REQUEST_EXTERNAL_STORAGE = 1
private val PERMISSIONS_STORAGE = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class MainActivity : AppCompatActivity() {

    private val apiService = APIClient.client.create(APIInterface::class.java)
    private lateinit var imageAdapter: ImageAdapter
    lateinit var photos: ArrayList<ResponsePhoto?>
    lateinit var myAsyncTask: DownloadImage

    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: RecyclerViewLoadMoreScroll

    //instance ImageViewModel
    private lateinit var imageViewModel: ImageViewModel

    //number page default = 1
    private var newPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init view model
        val factory = ImageViewModel(applicationContext)
        imageViewModel =
            ViewModelProviders.of(this, factory).get(ImageViewModel::class.java)

        if (requestPermission()) displayRecyclerView()


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
            startActivityForResult(intent, REQUEST_RESULT)
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

    //init recyclerview
    private fun initComponentView() {
        setAdapter()
        setLayoutManager()
        setRVScrollListener()
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

    // Verify image downloaded
    private fun compareImageDownloaded(images: ArrayList<ResponsePhoto?>) {
        val nameImageDownloaded = getListNameImageToGallery()
        if (nameImageDownloaded != null) {
            for (j in 0 until nameImageDownloaded.size) {
                for (i in 0 until images.size) {
                    if (images[i]?.id?.compareTo(nameImageDownloaded[j]) == 0) {
                        images[i]?.isDownload = true
                        break
                    }
                }
            }
        }
    }

    // Get list name image downloaded to gallery
    private fun getListNameImageToGallery(): ArrayList<String>? {
        val listNameImage = imageViewModel.loadImagesFromSDCard()

        for (i in 0 until listNameImage.size) {
            listNameImage[i] = listNameImage[i].replace(".jpg", "")
        }

        return listNameImage
    }

    /**
     *   Select multi image to downloaded and compare list image choose to download
     *   Remove image to select when this image downloaded
     * */
    private fun getChooseImages(): ArrayList<ResponsePhoto> {
        // list image choose to download
        val param = imageAdapter.getChooseImages()
        val nameImage = getListNameImageToGallery()
        if (nameImage != null) {
            if (nameImage.size != 0) {
                for (i in 0 until nameImage.size) {
                    for (j in 0 until param.size) {
                        if (param[j].id.compareTo(nameImage[i]) == 0) {
                            param.removeAt(j)
                            break
                        }
                    }
                }
            }
        }

        return param
    }

    // Download image
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
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) true
        else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PERMISSION_GRANTED
            ) true
            else {
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
                if (grantResults[0] == PERMISSION_GRANTED) displayRecyclerView()
                else if (grantResults[0] == PERMISSION_DENIED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                            createDialog(this, "Permission is necessary to use your app") { _, _ ->
                                startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                    }
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data?.extras != null) {
                    val lisImageDeleted = data.getStringArrayListExtra(REQUEST_RESULT_DATA)
                    updateListImage(lisImageDeleted)
                }
            }
        }
    }

    private fun displayRecyclerView() {
        imgDownload.visibility = View.VISIBLE
        if (!checkConnectInternet(this))
            Toast.makeText(this, "Internet is not Available", Toast.LENGTH_SHORT).show()
        else
            loadPhotos()
    }

    // Update state item recycler view when delete image in gallery
    private fun updateListImage(listImageDelete: ArrayList<String>) {
        val photoServers = imageAdapter.getAllImage()
        for (i in 0 until listImageDelete.size) {
            listImageDelete[i] = listImageDelete[i].replace(".jpg", "")
            for (j in 0 until photoServers.size) {
                if (photoServers[j]?.id?.compareTo(listImageDelete[i]) == 0) {
                    photoServers[j]?.isDownload = false
                    imageAdapter.notifyItemChanged(j)
                    imageAdapter.notifyItemRangeChanged(j, photoServers.size)
                }
            }
        }
    }

}