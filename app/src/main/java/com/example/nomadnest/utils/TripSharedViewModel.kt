package com.example.nomadnest.utils

import androidx.lifecycle.ViewModel

class TripSharedViewModel : ViewModel() {
    var selectedLocation: String? = null
    var selectedDate: String? = null
    var selectedBudget: String? = null
}
