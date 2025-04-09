package com.example.nomadnest

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val NOTIFICATION_ID = 123
    private var lastLocation: Location? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    private val database = FirebaseDatabase.getInstance().getReference("locations/$userId")

    override fun onCreate() {
        super.onCreate()

        Log.d("Start0000", "Service Started")
        Log.d("database000", "$database")
        Log.d("userId0000", "$userId")

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        startLocationUpdates()
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // every 10 seconds
            .setMinUpdateDistanceMeters(10f) // only if user moved 10+ meters
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    Log.d("onLocationResult0000", "Moved: ${location.latitude}, ${location.longitude}")
                    saveLocationToFirebase(location)
                }
            }
        }
    }


    private fun startLocationUpdates() {
        // Save last known location immediately
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("lastLocation0000", "Fetched last location: $location")
                    saveLocationToFirebase(location)
                    lastLocation = location
                } else {
                    Log.d("lastLocation0000", "Last location is null")
                }
            }
            .addOnFailureListener {
                Log.e("lastLocation0000", "Error fetching last location: ${it.message}")
            }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationRequest", "Missing permissions: ${e.message}")
        }
    }

    private fun saveLocationToFirebase(location: Location) {
        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )
        database.push().setValue(locationData)
            .addOnSuccessListener {
                Log.d("FirebaseUpload", "Location stored successfully")
            }
            .addOnFailureListener {
                Log.e("FirebaseUpload", "Failed to store location: ${it.message}")
            }
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("NomadNest is tracking your location")
            .setContentText("Getting live location updates...")
            .setSmallIcon(R.drawable.location_ic)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channelId = "location_channel"
        val channelName = "Location Tracking"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("ServiceStopped", "LocationForegroundService stopped")
    }
}