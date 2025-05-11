package com.example.nomadnest.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nomadnest.data.models.UserProfile

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    suspend fun getUser(userId: String): UserProfile?

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

}
