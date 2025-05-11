package com.example.nomadnest.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nomadnest.data.models.Trip
import com.example.nomadnest.data.models.UserProfile

@Database(entities = [UserProfile::class, Trip::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nomad_nest_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}