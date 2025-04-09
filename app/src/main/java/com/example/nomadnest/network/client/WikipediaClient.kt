package com.example.nomadnest.network.client

import com.example.nomadnest.network.api.WikipediaApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WikipediaClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://en.wikipedia.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: WikipediaApiService = retrofit.create(WikipediaApiService::class.java)
}
