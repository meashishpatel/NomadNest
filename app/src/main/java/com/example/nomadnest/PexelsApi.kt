package com.example.nomadnest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApi {
    @GET("search")
    fun getNaturePhotos(
        @Header("Authorization") apiKey: String,  // API Key Header
        @Query("query") query: String = "nature",
        @Query("per_page") perPage: Int = 5
    ): Call<PexelsResponse>
}
