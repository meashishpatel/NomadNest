package com.example.nomadnest.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nomadnest.data.db.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class TripSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val db = AppDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        val tripDao = db.tripDao()
        val unsyncedTrips = tripDao.getUnsyncedTrips()

        unsyncedTrips.forEach { trip ->
            try {
                val docId = "${trip.userId}_${trip.destination}"
                firestore.collection("tripDetails")
                    .document(docId)
                    .set(trip)
                    .await() // Use kotlinx-coroutines-play-services
                // Mark as synced
                tripDao.updateTrips(trip.copy(isSynced = true))
            } catch (e: Exception) {
                return Result.retry()
            }
        }

        return Result.success()
    }
}
