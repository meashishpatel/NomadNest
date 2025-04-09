package com.example.nomadnest.data.models

data class PexelsResponse(
    val photos: List<Photo>
)

data class Photo(
    val id: Int,
    val alt: String,
    val photographer: String,
    val src: Src
)

data class Src(
    val medium: String
)
