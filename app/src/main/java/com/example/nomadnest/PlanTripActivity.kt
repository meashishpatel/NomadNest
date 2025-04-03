package com.example.nomadnest

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nomadnest.databinding.ActivityMainBinding
import com.example.nomadnest.databinding.ActivityPlanTripBinding

class PlanTripActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanTripBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlanTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.planTripBtn.setOnClickListener{
            val intent = Intent(this, TripDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}