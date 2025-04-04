package com.example.nomadnest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nomadnest.databinding.FragmentPlanTrip4Binding


class PlanTripFragment4 : Fragment() {
    private var _binding: FragmentPlanTrip4Binding? = null  // Updated Binding Class
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}