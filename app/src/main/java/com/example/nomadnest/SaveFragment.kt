package com.example.nomadnest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.nomadnest.databinding.FragmentExploreFramentBinding
import com.example.nomadnest.databinding.FragmentSaveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SaveFragment : Fragment() {

    private var _binding: FragmentSaveBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tripAdapter: TripAdapter  // RecyclerView Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        val userId = auth.currentUser?.uid
        userId?.let { fetchUserTrips(it) }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter()
        binding.tripsRecyclerView.adapter = tripAdapter
    }

    private fun fetchUserTrips(userId: String) {
        firestore.collection("tripDetails")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val trips = snapshot.documents.mapNotNull { it.toObject(Trip::class.java) }
                tripAdapter.submitList(trips)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch trips", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}