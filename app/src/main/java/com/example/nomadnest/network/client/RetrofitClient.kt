package com.example.nomadnest.network.client

import com.example.nomadnest.network.api.LocationApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: LocationApiService = retrofit.create(LocationApiService::class.java)
}
