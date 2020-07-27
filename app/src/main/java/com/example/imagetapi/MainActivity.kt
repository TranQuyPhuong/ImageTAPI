@file:Suppress("DEPRECATION")

package com.example.imagetapi

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.imagetapi.adapter.ImageAdapter
import com.example.imagetapi.datamannage.APIClient
import com.example.imagetapi.datamannage.APIInterface
import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import com.example.imagetapi.global.checkConnectInternet
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.MalformedURLException
import java.net.URL


class MainActivity : AppCompatActivity() {

    private val apiService = APIClient.client.create(APIInterface::class.java)
    private lateinit var imageAdapter: ImageAdapter
    lateinit var photos: ArrayList<ResponsePhoto?>


    private lateinit var myAsyncTask: AsyncTask<ArrayList<URL>, Int, List<Bitmap>>


    //lateinit var loadMorePhoto: ArrayList<ResponsePhoto?>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    private lateinit var scrollListener: RecyclerViewLoadMoreScroll
    var newPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkConnectInternet(this))
            Toast.makeText(this, "Internet is not Available", Toast.LENGTH_SHORT).show()
        else
            loadPhotos()

        //listener click download button
        imgDownload.setOnClickListener {
            // init Async Task
            myAsyncTask = DownloadImage(this)
            downloadImage()
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
                initComponentView()
                newPage += 1
            }

        })
    }

    private fun initComponentView() {
        setAdapter()
        setLayoutManager()
        setRVScrollListener()
    }

    private fun allData(): ArrayList<ResponsePhoto?> = imageAdapter.getAllData()

    private fun downloadImage() {
        val urls: ArrayList<URL>? = ArrayList()
        var data = allData()
        for (i in 0 until data.size) {
            if (data[i]!!.isDownload) {
                val url: URL = stringToURL(data[i]?.url?.small)!!
                urls?.add(url)
            }
        }
        if (urls!=null) myAsyncTask.execute(urls)
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
                response.body()?.let { loadMoreData(it) }

            }

        })
    }

    fun loadMoreData(photos: ArrayList<ResponsePhoto?>) {
        //Add the Loading View
        imageAdapter.addLoadingView()
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

    }

}