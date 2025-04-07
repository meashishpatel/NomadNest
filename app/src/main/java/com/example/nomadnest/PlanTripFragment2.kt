package com.example.nomadnest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.nomadnest.databinding.FragmentPlanTrip1Binding
import com.example.nomadnest.databinding.FragmentPlanTrip2Binding


import java.text.SimpleDateFormat
import java.util.*

class PlanTripFragment2 : Fragment() {

    private var _binding: FragmentPlanTrip2Binding? = null
    private val binding get() = _binding!!

    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val sharedViewModel: TripSharedViewModel by activityViewModels()

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

        disableNextBtn()

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }

            val selectedDate = selectedCalendar.timeInMillis

            if (startDate == null || (startDate != null && endDate != null)) {
                // Start fresh
                startDate = selectedDate
                endDate = null
                binding.dateRangeTV.text = "Start Date: ${dateFormatter.format(startDate!!)}"
                disableNextBtn()
            } else if (selectedDate >= startDate!!) {
                endDate = selectedDate
                binding.dateRangeTV.text =
                    "Trip Dates: ${dateFormatter.format(startDate!!)} To ${dateFormatter.format(endDate!!)}"
                sharedViewModel.selectedDate =
                    "Trip Dates: ${dateFormatter.format(startDate!!)} To ${dateFormatter.format(endDate!!)}"
                enableNextBtn()
            } else {
                // If selected earlier than start date, treat it as new start
                startDate = selectedDate
                endDate = null
                binding.dateRangeTV.text = "Start Date: ${dateFormatter.format(startDate!!)}"
                disableNextBtn()
            }
        }

        binding.nextbtn.setOnClickListener {
            loadFragment(PlanTripFragment3())  // Load next step
        }
    }

    private fun enableNextBtn() {
        binding.nextbtn.isEnabled = true
        binding.nextbtn.isClickable = true
        binding.nextbtn.background = resources.getDrawable(R.drawable.blue_btn_bg, null)
        binding.nextText.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun disableNextBtn() {
        binding.nextbtn.isEnabled = false
        binding.nextbtn.isClickable = false
        binding.nextbtn.background = resources.getDrawable(R.drawable.grey_btn_bg, null)
        binding.nextText.setTextColor(resources.getColor(R.color.grey, null))
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? TripDetailsActivity)?.updateProgressBar(50)
    }
}