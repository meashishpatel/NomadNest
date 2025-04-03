package com.example.nomadnest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    val bio: String,
)