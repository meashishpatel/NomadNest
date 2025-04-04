package com.example.nomadnest


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.databinding.ActivityTripDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTripDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (savedInstanceState == null) {
            loadFragment(PlanTripFragment1())
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    loadFragment(ExploreFragment())
                    true
                }
                R.id.nav_save -> {
                    loadFragment(SaveFragment())
                    true
                }
                R.id.nav_plan -> {
                    loadFragment(PlanFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }


//        auth = FirebaseAuth.getInstance()
//        db = AppDatabase.getDatabase(this)
//        firestore = FirebaseFirestore.getInstance()
//
//        val userId = auth.currentUser?.uid ?: return

//        binding.saveTripBtn.setOnClickListener {
//            val tripName = binding.tripName.text.toString()
//            val destination = binding.destinationET.text.toString()
//            val date = binding.dateET.text.toString()
//            val budget = binding.budgetET.text.toString().toInt()
//            val trip = Trip(userId, tripName, destination, date, budget)
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                db.tripDao().insertTrip(trip)
//                if (isNetworkAvailable()) {
//                    syncTripDetails(trip)
//                }
//            }
//            Toast.makeText(this, "Trip updated successfully!", Toast.LENGTH_SHORT).show()
//        }

    }

//    private fun syncTripDetails(trip: Trip) {
//        firestore.collection("tripDetails").document(trip.userId)
//            .set(trip)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Trip synced to Firebase", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Failed to sync trip", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    private fun isNetworkAvailable(): Boolean {
//        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return false
//        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
//        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
//    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    fun updateProgressBar(progress: Int) {
        binding.progressBar.progress = progress
    }


}