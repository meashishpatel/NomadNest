package com.example.nomadnest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.nomadnest.databinding.FragmentPlanTrip2Binding
import com.example.nomadnest.databinding.FragmentPlanTrip3Binding


class PlanTripFragment3 : Fragment() {
    private var _binding: FragmentPlanTrip3Binding? = null  // Updated Binding Class
    private val binding get() = _binding!!

    private var selectedBudget: View? = null  // To track the selected view
    private val sharedViewModel: TripSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanTrip3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? TripDetailsActivity)?.updateProgressBar(75)

        // Handle Next Button Click
        binding.nextbtn.setOnClickListener {
            loadFragment(PlanTripFragment4())  // Load Next Fragment
        }
        disableNextBtn()

        setupBudgetOption(binding.cheapbtn, "Cheap")
        setupBudgetOption(binding.economicbtn, "Economic")
        setupBudgetOption(binding.luxurybtn, "Luxury")
        setupBudgetOption(binding.flexible, "Flexible")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        (activity as? TripDetailsActivity)?.updateProgressBar(75)  // Update progress when coming back
    }

    private fun setupBudgetOption(option: View, budgetValue: String) {
        option.setOnClickListener {
            // Reset previously selected background
            selectedBudget?.setBackgroundResource(R.drawable.default_card_bg)

            // Set new background for selected option
            option.setBackgroundResource(R.drawable.image_selected_border)
            selectedBudget = option

            // Store the selected budget in the shared ViewModel
            sharedViewModel.selectedBudget = budgetValue

            enableNextBtn()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)  // Replace Fragment
            .addToBackStack(null)  // Allow Back Navigation
            .commit()
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
}