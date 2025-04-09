package com.example.nomadnest.data.models

data class Category(
    val name: String,
    val iconRes: Int, // Resource ID for icon
    var isSelected: Boolean = false
)
