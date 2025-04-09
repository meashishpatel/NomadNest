package com.example.nomadnest.services
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.utils.scheduleTripNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BootReceiver : BroadcastReceiver() {


    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onReceive(context: Context, intent: Intent) {


        auth = FirebaseAuth.getInstance()
        var userId = auth.currentUser?.uid

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
            if (userId == null){
                userId = prefs.getString("userId", null)
            }
            // Re-schedule all trips stored in Room
            val db = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val trips = userId?.let { db.tripDao().getAllTripsForUser(it) }
                if (trips != null) {
                    for (trip in trips) {
                        scheduleTripNotification(context, trip)
                    }
                }else {
                    Log.d("BootReceiver", "User ID not found, skipping alarm scheduling.")
                }
            }
        }
    }
}
