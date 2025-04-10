package com.example.nomadnest.ui.trip

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nomadnest.R
import com.example.nomadnest.network.client.RetrofitClient
import com.example.nomadnest.utils.TripSharedViewModel
import com.example.nomadnest.adapters.LocationSuggestionAdapter
import com.example.nomadnest.data.models.LocationResponseItem
import com.example.nomadnest.databinding.FragmentPlanTrip1Binding
import com.example.nomadnest.ui.main.TripDetailsActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


class PlanTripFragment1 : Fragment() {

    private var _binding: FragmentPlanTrip1Binding? = null
    private val binding get() = _binding!!

    private lateinit var locationAdapter: LocationSuggestionAdapter
    private val suggestions = mutableListOf<LocationResponseItem>()
    private var debounceJob: Job? = null
    private var fetchJob: Job? = null
    private val sharedViewModel: TripSharedViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanTrip1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextbtn.isEnabled = false

        binding.locationSuggestionsRV.layoutManager = LinearLayoutManager(requireContext())

        (activity as? TripDetailsActivity)?.updateProgressBar(25)

        // Add the OnEditorActionListener for the search EditText
        binding.searchET.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                // Check if suggestions list is not empty
                if (suggestions.isNotEmpty()) {
                    // Select the first suggestion
                    val selectedSuggestion = suggestions[0]
                    binding.selectedLocationTV.text = "Selected: ${selectedSuggestion.displayName}"
                    sharedViewModel.selectedLocation = selectedSuggestion.displayName

                    // Hide the suggestions RecyclerView
                    binding.locationSuggestionsRV.visibility = View.GONE

                    // Hide the keyboard
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.searchET.windowToken, 0)

                    // Enable the Next button
                    enableNextBtn()
                }
                true // Indicate that the action was handled
            } else {
                false
            }
        }

        binding.searchET.doOnTextChanged { text, _, _, _ ->
            debounceJob?.cancel()
            if (!text.isNullOrBlank()) {
                debounceJob = lifecycleScope.launch {
                    delay(300) // Debounce delay
                    fetchLocationSuggestions(text.toString())
                }
            } else {
                binding.locationSuggestionsRV.visibility = View.GONE
                suggestions.clear()
                if (::locationAdapter.isInitialized) {
                    locationAdapter.notifyDataSetChanged()
                }
            }
        }

        // Handle Next Button Click
        binding.nextbtn.setOnClickListener {
            val fragment = PlanTripFragment4()
            loadFragment(PlanTripFragment2())  // Load Next Fragment
        }
        setupImageSelection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        (activity as? TripDetailsActivity)?.updateProgressBar(25)  // Update progress when coming back
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)  // Replace Fragment
            .addToBackStack(null)  // Allow Back Navigation
            .commit()
    }

    private fun fetchLocationSuggestions(query: String) {
        fetchJob?.cancel() // Cancel the last fetch if still running

        fetchJob = lifecycleScope.launch {
            try {
                val results = RetrofitClient.apiService.getLocationSuggestions(query)
                suggestions.clear()
                suggestions.addAll(results)

                if (!::locationAdapter.isInitialized) {
                    locationAdapter = LocationSuggestionAdapter(suggestions) { selected ->
                        binding.selectedLocationTV.text = "Selected: ${selected.displayName}"
                        sharedViewModel.selectedLocation = selected.displayName
                        binding.locationSuggestionsRV.visibility = View.GONE

                        enableNextBtn()
                    }
                    binding.locationSuggestionsRV.adapter = locationAdapter
                } else {
                    locationAdapter.notifyDataSetChanged()
                }

                binding.locationSuggestionsRV.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("LocationFetch", "Error: ${e.message}")
            }
        }
    }

    private fun setupImageSelection() {
        val imageMap = mapOf(
            binding.india to "India",
            binding.china to "China",
            binding.australia to "Australia",
            binding.norway to "Norway",
            binding.hungary to "Hungary",
            binding.eastAfrica to "East Africa"
        )

        var selectedImage: View? = null

        imageMap.forEach { (imageView, name) ->
            imageView.setOnClickListener {
                binding.selectedLocationTV.text = name
                sharedViewModel.selectedLocation = name
                selectedImage?.background = null
                imageView.setBackgroundResource(R.drawable.image_selected_border)
                selectedImage = imageView

                enableNextBtn()
            }
        }
    }

    private fun enableNextBtn() {
        binding.nextbtn.isEnabled = true
        binding.nextbtn.isClickable = true
        binding.nextbtn.background = resources.getDrawable(R.drawable.blue_btn_bg, null)
        binding.nextText.setTextColor(resources.getColor(R.color.white, null))
    }
}
