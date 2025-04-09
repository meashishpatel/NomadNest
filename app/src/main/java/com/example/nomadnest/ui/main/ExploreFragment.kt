package com.example.nomadnest.ui.main

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nomadnest.databinding.FragmentExploreFramentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.Manifest;
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.TimeZone
import androidx.core.view.isVisible
import com.example.nomadnest.ui.shared.LocationPermissionDialogFragment
import com.example.nomadnest.data.models.Photo
import com.example.nomadnest.R
import com.example.nomadnest.network.client.RetrofitClientWeather
import com.example.nomadnest.network.client.RetrofitInstancePexels
import com.example.nomadnest.network.client.WikipediaClient
import com.example.nomadnest.adapters.CategoryAdapter
import com.example.nomadnest.adapters.NearbyPlaceAdapter
import com.example.nomadnest.adapters.PhotoAdapter
import com.example.nomadnest.data.models.Category
import com.example.nomadnest.data.models.NearbyPlace
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ExploreFragment : Fragment(), OnMapReadyCallback{

    private var _binding: FragmentExploreFramentBinding? = null
    private val binding get() = _binding!!
    private val apiKey = "hNZUWMh0arq8aRNHLgNllKXcryGFHtNWX5VOL9S9PtxKm37nnnnPypQt"
    private val weatherApiKey = "ddc71934dab246249b9a80326c66ce06"

    private lateinit var googleMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val database = FirebaseDatabase.getInstance().getReference("locations")
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    private var trackingEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreFramentBinding.inflate(inflater, container, false)
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        trackingEnabled = prefs.getBoolean("location_tracking_enabled", false)
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        fetchAndDisplayLocations()
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        binding.recyclerViewPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val categories = listOf(
            Category("Adventure Travel", R.drawable.adventure, isSelected = true),
            Category("City Breaks", R.drawable.city),
            Category("See Nearby", R.drawable.location_ic),
            Category("Hiking", R.drawable.snow),
            Category("Sea", R.drawable.sea)
        )

        //  Map categories to API queries
        val categoryToQuery = mapOf(
            "Adventure Travel" to "mountains",
            "City Breaks" to "city",
            "See Nearby" to "seeNearby",
            "Hiking" to "hiking",
            "Sea" to "beach"
        )

        fun askForLocation() {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                if (isLocationEnabled()) {
                    toggleLocationCard()
                } else {
                    showEnableLocationDialog()
                }

            } else {
                showLocationPermissionDialog()
            }
        }

        // Set Default Images (for Adventure Travel)
        fetchPhotos(categoryToQuery["Adventure Travel"] ?: "nature", isSearch = false)

        val categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            val query = categoryToQuery[selectedCategory.name] ?: "nature"
            if(query == "seeNearby"){
                askForLocation()
            }
            else{
                fetchPhotos(query, isSearch = false)
                hideNearbyItems()
            }
        }

        binding.recyclerViewCategories.adapter = categoryAdapter

        binding.searchbox.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = binding.searchbox.text.toString().trim()
                if (searchQuery.isNotEmpty()) {
                    fetchPhotos(searchQuery, isSearch = true)

                    // 🔽 Hide the keyboard
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(binding.searchbox.windowToken, 0)
                    hideNearbyItems()
                }
                true
            } else {
                false
            }
        }
        binding.searchbtn.setOnClickListener{
            val searchQuery = binding.searchbox.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                fetchPhotos(searchQuery, isSearch = true)
                //  Hide the keyboard
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchbox.windowToken, 0)
                hideNearbyItems()
            }
        }

        val movement_mapFragment = childFragmentManager.findFragmentById(R.id.movement_map_fragment) as SupportMapFragment
        movement_mapFragment.getMapAsync(this)

    }

    private fun fetchPhotos(query: String, isSearch: Boolean) {
        lifecycleScope.launch {
            try {
                val api = RetrofitInstancePexels.getRetrofitInstance(apiKey)
                val response = api.searchPhotos(query)

                withContext(Dispatchers.Main) {
                    val photos = response.photos
                    binding.recyclerViewPhotos.adapter = PhotoAdapter(photos.map {
                        Photo(it.id, it.alt, it.photographer, it.src)
                    })

                    binding.recyclerViewPhotos.layoutManager =
                        if (isSearch) LinearLayoutManager(requireContext())
                        else LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                    binding.recyclerViewPhotos.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.categoryTV.text = query.capitalize()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLocationPermissionDialog() {
        val dialog = LocationPermissionDialogFragment{toggleLocationCard()}
        dialog.show(parentFragmentManager, "LocationPermissionDialog")
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toggleLocationCard()
        } else {
            Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleLocationCard() {
        if (binding.locationCard.isVisible) {
            hideNearbyItems()
            binding.recyclerViewPhotos.visibility = View.VISIBLE
        } else {
            fetchLocation()
            binding.locationCard.visibility = View.VISIBLE
            binding.youareatTV.visibility = View.VISIBLE
            binding.movementCard.visibility = View.VISIBLE
            binding.nearbyResults.visibility = View.VISIBLE
            binding.recyclerViewPhotos.visibility = View.GONE
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                binding.latLngText.text = "Lat, Lng: ${it.latitude}, ${it.longitude}"
                getAddress(it.latitude, it.longitude)
                // Set your desired location here
                val targetLocation = LatLng(it.latitude, it.longitude)
                addMarkerAndMoveCamera(targetLocation)
            } ?: run {
                Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddress(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            binding.locationText.text = "Location: ${addresses[0].getAddressLine(0)}"
            fetchWeather(lat, lng)
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val weatherResponse = RetrofitClientWeather.weatherService.getCurrentWeather(lat, lon, weatherApiKey)
                val tempCelsius = (weatherResponse.main.temp - 273.15).toInt()
                val feelsLikeC = (weatherResponse.main.feels_like - 273.15).toInt()
                val minC = (weatherResponse.main.temp_min - 273.15).toInt()
                val maxC = (weatherResponse.main.temp_max - 273.15).toInt()

                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                formatter.timeZone = TimeZone.getDefault()
                val places = fetchNearbyPlacesWithImages(lat, lon)
                places.forEach {
                    Log.d("NearbyPlace", "${it.title} - ${it.distance}m - ${it.thumbnailUrl}")
                }

                withContext(Dispatchers.Main) {
                    binding.weatherConditionText.text = "Condition: ${weatherResponse.weather[0].main}"
                    binding.weatherTempText.text = "Temp: $tempCelsius°C"
                    binding.weatherFeelsLike.text = "Feels like: $feelsLikeC°C"
                    binding.weatherHumidity.text = "Humidity: ${weatherResponse.main.humidity}%"
                    binding.weatherVisibility.text = "Visibility: ${weatherResponse.visibility ?: "N/A"} m"
                    binding.weatherWindSpeed.text = "Wind Speed: ${weatherResponse.wind.speed} m/s"
                    binding.weatherSunrise.text = "Sunrise: ${formatter.format(weatherResponse.sys.sunrise * 1000)}"
                    binding.weatherSunset.text = "Sunset: ${formatter.format(weatherResponse.sys.sunset * 1000)}"
                    binding.weatherSeaLevel.text = weatherResponse.main.sea_level?.let {
                        "Sea Level: $it hPa"
                    } ?: "Sea Level: N/A"
                    binding.weatherMinMax.text = "Min/Max: $minC / $maxC °C"

                    binding.weatherConditionText.visibility = View.VISIBLE
                    binding.weatherTempText.visibility = View.VISIBLE
                    binding.weatherBackground.visibility = View.VISIBLE

                    val condition = weatherResponse.weather[0].main.lowercase()
                    val backgroundRes = when (condition) {
                        "clear" -> R.drawable.sunny_bg
                        "clouds" -> R.drawable.cloudy_bg
                        "rain" -> R.drawable.rainy_bg
                        "snow" -> R.drawable.snow_bg
                        "thunderstorm" -> R.drawable.thunder_bg
                        else -> R.drawable.default_weather_bg
                    }
                    binding.weatherBackground.setImageResource(backgroundRes)

                    val adapter = NearbyPlaceAdapter(places)
                    binding.nearbyResults.adapter = adapter
                    binding.nearbyResults.layoutManager = LinearLayoutManager(requireContext())
                    binding.nearbyResults.visibility = View.VISIBLE
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch weather: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchNearbyPlacesWithImages(lat: Double, lon: Double): List<NearbyPlace> {
        return try {
            val coord = "$lat|$lon"
            val geoResponse = WikipediaClient.api.getNearbyPlaces(coord = coord)

            val places = geoResponse.query.geosearch
            val titles = places.joinToString("|") { it.title }

            val imageResponse = WikipediaClient.api.getPageImages(titles = titles)

            val imageMap = imageResponse.query.pages

            return places.map { place ->
                val thumbnailUrl = imageMap.values.find { it.title == place.title }?.thumbnail?.source
                NearbyPlace(title = place.title, distance = place.dist, thumbnailUrl = thumbnailUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    private fun addMarkerAndMoveCamera(location: LatLng) {
        googleMap.clear() // Clear previous markers
        googleMap.addMarker(MarkerOptions().position(location).title("You"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
    private fun hideNearbyItems(){
        binding.nearbyResults.visibility = View.GONE
        binding.locationCard.visibility = View.GONE
        binding.youareatTV.visibility = View.GONE
        binding.youractivityTV.visibility = View.GONE
        binding.movementCard.visibility = View.GONE
    }

    private fun fetchAndDisplayLocations() {
        if (!trackingEnabled) {
            Log.d("LocationDisabled", "Turn on location")
            binding.mapfragment.visibility = View.GONE
            binding.enablelocation.visibility = View.VISIBLE
            return
        }
        database.child(userId).get().addOnSuccessListener { snapshot ->
            val uniquePointKeys = mutableSetOf<String>()
            val points = mutableListOf<LatLng>()

            for (child in snapshot.children) {
                val lat = child.child("latitude").getValue(Double::class.java)
                val lng = child.child("longitude").getValue(Double::class.java)
                if (lat != null && lng != null) {
                    val key = "$lat,$lng"
                    if (uniquePointKeys.add(key)) {
                        points.add(LatLng(lat, lng))
                    }
                }
            }

            if (points.isNotEmpty()) {
                // Move camera to the first unique point
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 12f))

                Log.d("Map Points", "$points")

                // Draw path
                val polylineOptions = PolylineOptions().addAll(points).color(Color.BLUE).width(5f)
                googleMap.addPolyline(polylineOptions)
            }
        }.addOnFailureListener {
            Log.e("MapFirebase", "Error fetching locations: ${it.message}")
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Location")
            .setMessage("Your location is turned off. Please enable it in settings to use this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
    }
}