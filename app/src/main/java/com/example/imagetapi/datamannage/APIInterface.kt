package com.example.imagetapi.datamannage

import com.example.imagetapi.datamannage.dataclass.ResponsePhoto
import com.example.imagetapi.global.keyAccess
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface APIInterface {

    @GET("photos")
    fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("order_by") orderBy: String = "latest",
        @Query("client_id") clientID: String = keyAccess
    ): Call<ArrayList<ResponsePhoto?>>

}