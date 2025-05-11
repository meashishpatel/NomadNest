package com.example.nomadnest.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String = "",
    val name: String = "",
    val email: String = "",
    val country: String = "",
    val phone: String = "",
    val gender: String = "",
    val bio: String = ""
)
