package com.example.nomadnest.ui.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.R
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.data.models.UserProfile
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
        var gender = "Prefer not to say"

        val genderOptions = arrayOf("Prefer not to say", "Male", "Female", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genderSpinner.adapter = adapter

        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                // Get the selected gender
                gender = parentView.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }


        val userId = auth.currentUser?.uid ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            if (!isNetworkAvailable()){
                val user = db.userDao().getUser(userId)
                if (user != null) {
                    runOnUiThread {
                        binding.nameET.setText(user.name)
                        binding.emailET.setText(user.email)
                        binding.phoneET.setText(user.phone)
                        binding.bioET.setText(user.bio)
                    }
                }
            } else {
                // Room is empty — fetch from Firestore
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val fetchedUser = document.toObject(UserProfile::class.java)
                            fetchedUser?.let {
                                binding.nameET.setText(it.name)
                                binding.emailET.setText(it.email)
                                binding.phoneET.setText(it.phone)
                                binding.bioET.setText(it.bio)

                                // Save to Room
                                lifecycleScope.launch(Dispatchers.IO) {
                                    db.userDao().insertUser(it)
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ProfileActivity, "Failed to load profile from Firebase", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.updateProfileBtn.setOnClickListener {
            val name = binding.nameET.text.toString()
            val email = binding.emailET.text.toString()
            val phone = binding.phoneET.text.toString()
            val bio = binding.bioET.text.toString()
            val country = binding.countryET.text.toString()
            val userProfile = UserProfile(userId, name, email, country, phone,gender, bio)

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
//                Toast.makeText(this, "Profile synced to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
//                Toast.makeText(this, "Failed to sync profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}