package com.example.nomadnest.data.models

data class NearbyPlace(
    val title: String,
    val distance: Double,
    val thumbnailUrl: String?
)

data class GeoSearchResult(
    val query: GeoQuery
)

data class GeoQuery(
    val geosearch: List<GeoPage>
)

data class GeoPage(
    val title: String,
    val dist: Double
)

data class PageImageResult(
    val query: PageQuery
)

data class PageQuery(
    val pages: Map<String, PageDetail>
)

data class PageDetail(
    val title: String,
    val thumbnail: Thumbnail?
)

data class Thumbnail(
    val source: String,
    val width: Int,
    val height: Int
)