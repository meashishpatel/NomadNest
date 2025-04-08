package com.example.nomadnest

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.service.notification.NotificationListenerService
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.app.Service
import android.os.IBinder
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Priority


class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "location_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                notificationChannelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Tracking Location")
            .setContentText("Your location is being shared in real-time")
            .setSmallIcon(R.drawable.location_ic)
            .setOngoing(true)

        return builder.build()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5 * 60 * 1000 // Every 5 minutes
            fastestInterval = 1 * 60 * 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        uploadLocationToFirebase(location)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    private fun uploadLocationToFirebase(location: Location) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val locationData = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance()
            .getReference("locations")
            .child(uid)
            .setValue(locationData)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
