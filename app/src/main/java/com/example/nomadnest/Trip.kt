package com.example.nomadnest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey val userId: String,
    val destination: String,
    val date: String,
    val budget: String
)