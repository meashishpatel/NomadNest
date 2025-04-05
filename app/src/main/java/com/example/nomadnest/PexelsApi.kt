package com.example.nomadnest

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApi {
    @GET("search")
    suspend fun getPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 5
    ): PexelsResponse
}
