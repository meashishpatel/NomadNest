package com.example.nomadnest.utils

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.nomadnest.data.models.Trip
import com.example.nomadnest.services.TripAlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

fun scheduleTripNotification(context: Context, trip: Trip) {
    try {
        // Handle custom date format like "Trip Dates: 07 Apr 2025 To 15 Apr 2025"
        val rawDate = trip.date
        val startDateStr = rawDate
            .replace("Trip Dates:", "")
            .split("To")
            .getOrNull(0)
            ?.trim() ?: return

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val tripTimeMillis = dateFormat.parse(startDateStr)?.time ?: return

        if (tripTimeMillis < System.currentTimeMillis()) {
            Log.d("TripAlarm", "Skipping trip in the past: ${trip.destination}")
            return
        }

        // Check for exact alarm permission on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("TripAlarm", "Exact alarm permission not granted. Requesting user to allow it.")
                // Open settings so user can manually allow it
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        val alarmIntent = Intent(context, TripAlarmReceiver::class.java).apply {
            putExtra("destination", trip.destination)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            trip.destination.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tripTimeMillis,
            pendingIntent
        )

        Log.d("TripAlarm", "Alarm set for ${trip.destination} at $tripTimeMillis")

    } catch (e: Exception) {
        Log.e("TripAlarm", "Failed to schedule trip notification: ${e.message}")
    }
}
