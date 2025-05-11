package com.example.nomadnest.ui.trip

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.nomadnest.R
import com.example.nomadnest.utils.TripSharedViewModel
import com.example.nomadnest.databinding.FragmentPlanTrip2Binding
import com.example.nomadnest.ui.main.TripDetailsActivity
import com.google.android.material.datepicker.MaterialDatePicker
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

        binding.calendarView.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select your travel dates")
                .build()

            dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                startDate = selection.first
                endDate = selection.second

                val formattedStart = dateFormatter.format(Date(startDate!!))
                val formattedEnd = dateFormatter.format(Date(endDate!!))

                val selectedRange = "Trip Dates: $formattedStart To $formattedEnd"
                binding.dateRangeTV.text = selectedRange
                sharedViewModel.selectedDate = selectedRange

                enableNextBtn()
            }
        }


        binding.nextbtn.setOnClickListener {
            loadFragment(PlanTripFragment3()) // Load next step
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
        if(sharedViewModel.selectedDate != null){
            binding.dateRangeTV.text = "Selected: ${sharedViewModel.selectedDate}"
            enableNextBtn()
        }
    }
}