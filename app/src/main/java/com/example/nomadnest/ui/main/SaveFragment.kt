package com.example.nomadnest.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.R
import com.example.nomadnest.adapters.TripAdapter
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.data.db.TripDao
import com.example.nomadnest.data.models.Trip
import com.example.nomadnest.databinding.FragmentSaveBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class SaveFragment : Fragment() {

    private var _binding: FragmentSaveBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tripAdapter: TripAdapter  // RecyclerView Adapter
    private lateinit var tripDao: TripDao


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
        tripDao = AppDatabase.getDatabase(requireContext()).tripDao()


        setupRecyclerView()
        val userId = auth.currentUser?.uid
        userId?.let { fetchUserTrips(it) }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onUpdateClick = { trip -> showUpdateDialog(trip) },
            onDeleteClick = { trip -> deleteTrip(trip) }
        )
        binding.tripsRecyclerView.adapter = tripAdapter

        binding.tripsRecyclerView.adapter = tripAdapter
    }

    private fun deleteTrip(trip: Trip) {
        firestore.collection("tripDetails")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .whereEqualTo("destination", trip.destination)
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    document.reference.delete()
                }
                fetchUserTrips(auth.currentUser!!.uid)
                Toast.makeText(requireContext(), "Trip deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete trip", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showUpdateDialog(trip: Trip) {
        val context = requireContext()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_trip, null)
        val destinationEditText = dialogView.findViewById<EditText>(R.id.editDestination)
        val dateEditText = dialogView.findViewById<EditText>(R.id.editDate)
        val budgetEditText = dialogView.findViewById<EditText>(R.id.editBudget)

        destinationEditText.setText(trip.destination)
        dateEditText.setText(trip.date)
        budgetEditText.setText(trip.budget)

        AlertDialog.Builder(context)
            .setTitle("Update Trip")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedTrip = trip.copy(
                    destination = destinationEditText.text.toString(),
                    date = dateEditText.text.toString(),
                    budget = budgetEditText.text.toString()
                )

                firestore.collection("tripDetails")
                    .whereEqualTo("userId", auth.currentUser?.uid)
                    .whereEqualTo("destination", trip.destination)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot.documents) {
                            document.reference.set(updatedTrip)
                        }
                        fetchUserTrips(auth.currentUser!!.uid)
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun fetchUserTrips(userId: String) {
        firestore.collection("tripDetails")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val trips = snapshot.documents.mapNotNull { it.toObject(Trip::class.java) }
                tripAdapter.submitList(trips)
                saveTripsToRoom(trips)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch trips", Toast.LENGTH_SHORT).show()
            }
    }
    private fun saveTripsToRoom(trips: List<Trip>) {
        lifecycleScope.launch {
            try {
                for (trip in trips) {
                    tripDao.insertTrip(trip.copy(isSynced = true))
                }
            } catch (e: Exception) {
                Log.e("RoomDB", "Error saving trips to Room", e)
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}