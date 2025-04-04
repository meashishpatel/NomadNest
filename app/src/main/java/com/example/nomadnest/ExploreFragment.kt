package com.example.nomadnest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nomadnest.databinding.FragmentExploreFramentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreFramentBinding? = null
    private val binding get() = _binding!!
    private val apiKey = "hNZUWMh0arq8aRNHLgNllKXcryGFHtNWX5VOL9S9PtxKm37nnnnPypQt"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreFramentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        fetchPhotos()
    }

    private fun fetchPhotos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.pexels.com/v1/search?query=nature&per_page=5")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", apiKey)

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)
                val photosArray = jsonObject.getJSONArray("photos")
                Log.d("response", "$response")
                Log.d("jsonObject", "$jsonObject")
                Log.d("photosArray", "$photosArray")

                val photos = mutableListOf<Photo>()

                for (i in 0 until photosArray.length()) {
                    val photoObj = photosArray.getJSONObject(i)
                    val id = photoObj.getInt("id")
                    val alt = photoObj.getString("alt")
                    val photographer = photoObj.getString("photographer")
                    val url = photoObj.getJSONObject("src").getString("medium")

                    photos.add(Photo(id, alt, photographer, url))
                }

                withContext(Dispatchers.Main) {
                    binding.recyclerView.adapter = PhotoAdapter(photos)
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
}
