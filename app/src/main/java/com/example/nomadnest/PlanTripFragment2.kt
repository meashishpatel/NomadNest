package com.example.nomadnest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nomadnest.databinding.FragmentPlanTrip1Binding
import com.example.nomadnest.databinding.FragmentPlanTrip2Binding


class PlanTripFragment2 : Fragment() {
    private var _binding: FragmentPlanTrip2Binding? = null  // Updated Binding Class
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanTrip2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? TripDetailsActivity)?.updateProgressBar(50)

        // Handle Next Button Click
        binding.nextbtn.setOnClickListener {
            loadFragment(PlanTripFragment3())  // Load Next Fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        (activity as? TripDetailsActivity)?.updateProgressBar(50)  // Update progress when coming back
    }
    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)  // Replace Fragment
            .addToBackStack(null)  // Allow Back Navigation
            .commit()
    }
}