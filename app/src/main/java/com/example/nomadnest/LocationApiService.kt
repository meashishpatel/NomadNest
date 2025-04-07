package com.example.nomadnest

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationApiService {
    @GET("search")
    suspend fun getLocationSuggestions(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5
    ): List<LocationResponseItem>
}
