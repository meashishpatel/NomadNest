package com.example.nomadnest

import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaApiService {

    @GET("w/api.php")
    suspend fun getNearbyPlaces(
        @Query("action") action: String = "query",
        @Query("list") list: String = "geosearch",
        @Query("gscoord") coord: String,
        @Query("gsradius") radius: Int = 10000,
        @Query("format") format: String = "json"
    ): GeoSearchResult

    @GET("w/api.php")
    suspend fun getPageImages(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "pageimages",
        @Query("titles") titles: String,
        @Query("format") format: String = "json",
        @Query("pithumbsize") size: Int = 500
    ): PageImageResult
}
