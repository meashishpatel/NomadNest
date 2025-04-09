package com.example.nomadnest.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nomadnest.data.models.Trip

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip)

    @Query("SELECT * FROM trips WHERE userId = :userId")
    fun getAllTripsForUser(userId: String): List<Trip>

    @Query("SELECT * FROM trips WHERE destination = :destination LIMIT 1")
    suspend fun getTripByDestination(destination: String): Trip?

    @Query("DELETE FROM trips WHERE userId = :userId")
    suspend fun deleteTripsForUser(userId: String)

    @Query("SELECT * FROM trips WHERE isSynced = 0")
    suspend fun getUnsyncedTrips(): List<Trip>

    @Update
    suspend fun updateTrips(vararg trips: Trip)
}
