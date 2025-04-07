package com.example.nomadnest

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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import android.Manifest;
import android.app.AlertDialog
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ExploreFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentExploreFramentBinding? = null
    private val binding get() = _binding!!
    private val apiKey = "hNZUWMh0arq8aRNHLgNllKXcryGFHtNWX5VOL9S9PtxKm37nnnnPypQt"
    private val weatherApiKey = "ddc71934dab246249b9a80326c66ce06"

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreFramentBinding.inflate(inflater, container, false)
        return binding.root
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

        // ✅ Map categories to API queries
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
                toggleLocationCard()
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
            fetchPhotos(query, isSearch = false) // Fetch new images based on selection
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
                }
                true
            } else {
                false
            }
        }
    }

    private fun fetchPhotos(query: String, isSearch: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.pexels.com/v1/search?query=$query&per_page=10")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", apiKey)

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val photosArray = jsonObject.getJSONArray("photos")

                val photos = mutableListOf<Photo>()
                for (i in 0 until photosArray.length()) {
                    val photoObj = photosArray.getJSONObject(i)
                    val id = photoObj.getInt("id")
                    val alt = photoObj.getString("alt")
                    val photographer = photoObj.getString("photographer")
                    val imageUrl = photoObj.getJSONObject("src").getString("medium")

                    photos.add(Photo(id, alt, photographer, imageUrl))
                }

                withContext(Dispatchers.Main) {
                    binding.recyclerViewPhotos.adapter = PhotoAdapter(photos)
                    if (isSearch) {
                        binding.recyclerViewPhotos.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerViewPhotos.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewPhotos.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        binding.recyclerViewPhotos.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLocationPermissionDialog() {
        val dialog = LocationPermissionDialogFragment()
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
        if (binding.locationCard.visibility == View.VISIBLE) {
            binding.locationCard.visibility = View.GONE
        } else {
            binding.locationCard.visibility = View.VISIBLE
            fetchLocation()
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
                val url = URL("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$weatherApiKey")
                val connection = url.openConnection() as HttpURLConnection
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                val weatherArray = jsonObject.getJSONArray("weather")
                val weatherMain = weatherArray.getJSONObject(0).getString("main")

                val main = jsonObject.getJSONObject("main")
                val tempKelvin = main.getDouble("temp")
                val feelsLike = main.getDouble("feels_like")
                val tempMin = main.getDouble("temp_min")
                val tempMax = main.getDouble("temp_max")
                val humidity = main.getInt("humidity")
                val seaLevel = main.optInt("sea_level", -1)

                val visibility = jsonObject.optInt("visibility", -1)
                val windSpeed = jsonObject.getJSONObject("wind").getDouble("speed")

                val sys = jsonObject.getJSONObject("sys")
                val sunrise = sys.getLong("sunrise")
                val sunset = sys.getLong("sunset")

                val tempCelsius = (tempKelvin - 273.15).toInt()
                val feelsLikeC = (feelsLike - 273.15).toInt()
                val minC = (tempMin - 273.15).toInt()
                val maxC = (tempMax - 273.15).toInt()

                val formatter = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                formatter.timeZone = java.util.TimeZone.getDefault()

                withContext(Dispatchers.Main) {
                    binding.weatherConditionText.text = "Condition: $weatherMain"
                    binding.weatherTempText.text = "Temp: $tempCelsius°C"
                    binding.weatherFeelsLike.text = "Feels like: $feelsLikeC°C"
                    binding.weatherHumidity.text = "Humidity: $humidity%"
                    binding.weatherVisibility.text = "Visibility: ${if (visibility != -1) "$visibility m" else "N/A"}"
                    binding.weatherWindSpeed.text = "Wind Speed: $windSpeed m/s"
                    binding.weatherSunrise.text = "Sunrise: ${formatter.format(sunrise * 1000)}"
                    binding.weatherSunset.text = "Sunset: ${formatter.format(sunset * 1000)}"
                    binding.weatherSeaLevel.text = if (seaLevel != -1) "Sea Level: $seaLevel hPa" else "Sea Level: N/A"
                    binding.weatherMinMax.text = "Min/Max: $minC / $maxC °C"

                    binding.weatherConditionText.visibility = View.VISIBLE
                    binding.weatherTempText.visibility = View.VISIBLE
                    binding.weatherBackground.visibility = View.VISIBLE

                    val backgroundRes = when (weatherMain.lowercase()) {
                        "clear" -> R.drawable.sunny_bg
                        "clouds" -> R.drawable.cloudy_bg
                        "rain" -> R.drawable.rainy_bg
                        "snow" -> R.drawable.snow_bg
                        "thunderstorm" -> R.drawable.thunder_bg
                        else -> R.drawable.default_weather_bg
                    }

                    binding.weatherBackground.setImageResource(backgroundRes)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to fetch weather: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
    }

    companion object {
        private const val LOCATION_REQUEST_CODE = 1001
    }
}