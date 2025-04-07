package com.example.nomadnest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nomadnest.databinding.FragmentSaveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SaveFragment : Fragment() {

    private var _binding: FragmentSaveBinding? = null
    private val binding get() = _binding!!

    private lateinit var tripDao: TripDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tripAdapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val db = AppDatabase.getDatabase(requireContext())

        lifecycleScope.launch(Dispatchers.IO) {
            val localTrips = db.tripDao().getAllTripsForUser(userId)

            // Now update the UI safely on the Main thread
            launch(Dispatchers.Main) {
                if (localTrips.isNotEmpty()) {
                    showTripsInRecyclerView(localTrips)
                } else {
                    fetchTripsFromFirebase(userId)
                }
            }
        }
    }

    private fun showTripsInRecyclerView(trips: List<Trip>) {
        val adapter = TripAdapter(trips)
        binding.tripsRecyclerView.adapter = adapter
        binding.tripsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchTripsFromFirebase(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("tripDetails")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val trips = documents.mapNotNull { it.toObject(Trip::class.java) }
                showTripsInRecyclerView(trips)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load trips", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}