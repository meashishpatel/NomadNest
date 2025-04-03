package com.example.nomadnest

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getDatabase(this)
        firestore = FirebaseFirestore.getInstance()


        val userId = auth.currentUser?.uid ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUser(userId)
            user?.let {
                runOnUiThread {
                    binding.nameET.setText(it.name)
                    binding.emailET.setText(it.email)
                    binding.phoneET.setText(it.phone)
                    binding.bioET.setText(it.bio)
                }
            }
        }

        binding.updateProfileBtn.setOnClickListener {
            val name = binding.nameET.text.toString()
            val email = binding.emailET.text.toString()
            val phone = binding.phoneET.text.toString()
            val bio = binding.bioET.text.toString()
            val userProfile = UserProfile(userId, name, email, phone, bio)

            lifecycleScope.launch(Dispatchers.IO) {
                db.userDao().insertUser(userProfile)
                if (isNetworkAvailable()) {
                    syncUserProfile(userProfile)
                }
            }
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncUserProfile(userProfile: UserProfile) {
        firestore.collection("users").document(userProfile.userId)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile synced to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to sync profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}