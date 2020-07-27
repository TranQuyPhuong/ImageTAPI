package com.example.imagetapi.datamannage

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object APIClient {

    private var retrofit: Retrofit? = null

    val client: Retrofit
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("https://api.unsplash.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!
        }

}