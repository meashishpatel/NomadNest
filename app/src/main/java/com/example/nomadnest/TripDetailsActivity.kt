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

    private var currentFragmentIndex = 1


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
            loadFragment(1)
        }

        binding.backbtn.setOnClickListener {
            when (currentFragmentIndex) {
                1 -> finish()
                2 -> loadFragment(1)
                3 -> loadFragment(2)
                4 -> loadFragment(3)
            }
        }

    }

    private fun loadFragment(index: Int) {
        val fragment = when (index) {
            1 -> PlanTripFragment1()
            2 -> PlanTripFragment2()
            3 -> PlanTripFragment3()
            4 -> PlanTripFragment4()
            else -> PlanTripFragment1()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        currentFragmentIndex = index
    }

    fun updateProgressBar(progress: Int) {
        binding.progressBar.progress = progress
    }


}