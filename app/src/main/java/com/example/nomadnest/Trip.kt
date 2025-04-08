package com.example.nomadnest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    val userId: String = "",
    @PrimaryKey val destination: String = "",
    val date: String = "",
    val budget: String = "",
)