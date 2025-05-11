package com.example.nomadnest.network.api

import com.example.nomadnest.data.models.PexelsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PexelsApi {
    @GET("search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 10
    ): PexelsResponse
}
