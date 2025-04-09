package com.example.nomadnest.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.R
import com.example.nomadnest.auth.LoginActivity
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.databinding.ActivityAccountAndSecurityBinding
import com.example.nomadnest.services.LocationForegroundService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AccountAndSecurity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountAndSecurityBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountAndSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val db = AppDatabase.getDatabase(applicationContext)


        // Restore saved switch state
        val isTrackingEnabled = prefs.getBoolean("location_tracking_enabled", false)
        binding.locationTrackingSwitch.isChecked = isTrackingEnabled

        // Handle switch toggle
        binding.locationTrackingSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("location_tracking_enabled", isChecked).apply()

            val intent = Intent(this, LocationForegroundService::class.java)
            if (isChecked) {
                startService(intent)
            } else {
                stopService(intent)
            }
        }
        binding.deleteAccountButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                showDeleteConfirmation(user)
            }
        }
    }
    private fun showDeleteConfirmation(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ ->
            deleteUserAccount(user)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }



    private fun deleteUserAccount(user: FirebaseUser) {
        val userId = user.uid

        lifecycleScope.launch {
            // 1. Delete from Room DB
            val db = AppDatabase.getDatabase(applicationContext)
            db.userDao().deleteUser(userId)
            db.tripDao().deleteTripsForUser(userId)

            // 2. Delete from Firebase Auth
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 3. Clear shared prefs
                    getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()

                    // 4. Navigate to login or onboarding
                    val intent = Intent(this@AccountAndSecurity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Could not delete account"
                    Toast.makeText(this@AccountAndSecurity, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}