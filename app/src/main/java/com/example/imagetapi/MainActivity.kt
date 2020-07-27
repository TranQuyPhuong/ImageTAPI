package com.example.imagetapi

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

class MainActivity : AppCompatActivity() {

    private val apiService = APIClient.client.create(APIInterface::class.java)
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var mOnLoadMoreListener: OnLoadMoreListener
    lateinit var photos: ArrayList<ResponsePhoto?>

    //lateinit var loadMorePhoto: ArrayList<ResponsePhoto?>
    private lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    var newPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!checkConnectInternet(this))
            Toast.makeText(this, "Internet is not Available", Toast.LENGTH_SHORT).show()
        else
            loadPhotos()

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

    private fun downloadImage() {
        imgDownload.setOnClickListener {
            var listImage = imageAdapter.getAllData()

        }
    }

    private fun setAdapter() {
        imageAdapter = ImageAdapter(photos)
        imageAdapter.notifyDataSetChanged()
        recyclerImage.adapter = imageAdapter
    }

    fun setOnLoadMoreListener(mOnLoadMoreListener: OnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener
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
//        //Create the loadMoreItemsCells Arraylist
//        loadMorePhoto = ArrayList()
//        //Use Handler if the items are loading too fast.
//        //If you remove it, the data will load so fast that you can't even see the LoadingView
//        loadMorePhoto.addAll(photos)
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