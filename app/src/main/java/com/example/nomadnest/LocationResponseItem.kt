package com.example.nomadnest

import com.google.gson.annotations.SerializedName

data class LocationResponseItem(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String
)
