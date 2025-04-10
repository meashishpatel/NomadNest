package com.example.nomadnest.ui.main


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.nomadnest.R
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.databinding.ActivityTripDetailsBinding
import com.example.nomadnest.ui.trip.PlanFragment
import com.example.nomadnest.ui.trip.PlanTripFragment1
import com.example.nomadnest.ui.trip.PlanTripFragment2
import com.example.nomadnest.ui.trip.PlanTripFragment3
import com.example.nomadnest.ui.trip.PlanTripFragment4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TripDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripDetailsBinding

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

        binding.backbtn.setOnClickListener{
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            when (currentFragment){
                is PlanTripFragment1 ->{
                    finish()
                    loadFragment(PlanFragment())
                }
                is PlanTripFragment2 ->{
                    loadFragment(PlanTripFragment1())
                }
                is PlanTripFragment3 ->{
                    loadFragment(PlanTripFragment2())
                }
                is PlanTripFragment4 ->{
                    loadFragment(PlanTripFragment3())
                }
            }
        }

        if (savedInstanceState == null) {
            loadFragment(PlanTripFragment1())
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun updateProgressBar(progress: Int) {
        binding.progressBar.progress = progress
    }
}