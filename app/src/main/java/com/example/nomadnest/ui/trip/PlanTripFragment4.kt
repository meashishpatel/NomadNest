package com.example.nomadnest.ui.trip

import android.app.AlarmManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.nomadnest.databinding.FragmentPlanTrip4Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.lifecycleScope
import com.example.nomadnest.utils.TripSharedViewModel
import com.example.nomadnest.data.db.AppDatabase
import com.example.nomadnest.data.models.Trip
import com.example.nomadnest.utils.scheduleTripNotification
import com.example.nomadnest.ui.main.HomeActivity
import com.example.nomadnest.ui.main.TripDetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlanTripFragment4 : Fragment() {

    private var _binding: FragmentPlanTrip4Binding? = null  // Updated Binding Class
    private val binding get() = _binding!!
    private val sharedViewModel: TripSharedViewModel by activityViewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanTrip4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? TripDetailsActivity)?.updateProgressBar(100)

        auth = FirebaseAuth.getInstance()
        db = AppDatabase.getDatabase(requireContext())
        firestore = FirebaseFirestore.getInstance()
        updateTripDetailsUI(sharedViewModel)


        binding.confirmbtn.setOnClickListener{
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val location = sharedViewModel.selectedLocation
            val date = sharedViewModel.selectedDate
            val budget = sharedViewModel.selectedBudget
            val trip = Trip(
                userId = userId,
                destination = location ?: "Unknown",
                date = date ?: "N/A",
                budget = budget ?: "N/A",
            )
            lifecycleScope.launch(Dispatchers.IO) {
                db.tripDao().insertTrip(trip)

                if (isNetworkAvailable()) {
                    syncTripDetails(trip)
                }
            }
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData(Uri.parse("package:" + requireContext().packageName))
                    requireContext().startActivity(intent)
                }
            }

            scheduleTripNotification(requireContext(), trip)
        }

        binding.editdestination.setOnClickListener {
            showInputDialog("Edit Destination", sharedViewModel.selectedLocation ?: "") { newText ->
                sharedViewModel.selectedLocation = newText
                updateTripDetailsUI(sharedViewModel)
            }
        }

        binding.editdate.setOnClickListener {
            showInputDialog("Edit Date", sharedViewModel.selectedDate ?: "") { newText ->
                sharedViewModel.selectedDate = newText
                updateTripDetailsUI(sharedViewModel)
            }
        }

        binding.editbudget.setOnClickListener {
            showInputDialog("Edit Budget", sharedViewModel.selectedBudget ?: "") { newText ->
                sharedViewModel.selectedBudget = newText
                updateTripDetailsUI(sharedViewModel)
            }
        }
    }

    private fun updateTripDetailsUI(sharedViewModel: TripSharedViewModel){
        binding.destinationTV.text = sharedViewModel.selectedLocation
        binding.dateTV.text = sharedViewModel.selectedDate
        binding.budgetTV.text = sharedViewModel.selectedBudget
    }

        private fun syncTripDetails(trip: Trip) {
            firestore.collection("tripDetails")
                .document(trip.userId + "_" + trip.destination)
                .set(trip)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Trip synced to Firebase", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to sync trip", Toast.LENGTH_SHORT).show()
            }
        }

        private fun isNetworkAvailable(): Boolean {
            val connectivityManager =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        }

    private fun showInputDialog(title: String, currentText: String, onTextChanged: (String) -> Unit) {
        val context = requireContext()
        val inputEditText = android.widget.EditText(context)
        inputEditText.setText(currentText)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setView(inputEditText)
            .setPositiveButton("OK") { _, _ ->
                val newText = inputEditText.text.toString()
                onTextChanged(newText)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}