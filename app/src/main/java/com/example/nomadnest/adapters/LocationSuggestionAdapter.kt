package com.example.nomadnest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nomadnest.data.models.LocationResponseItem
import com.example.nomadnest.databinding.ItemLocationBinding

class LocationSuggestionAdapter(
    private val suggestions: List<LocationResponseItem>,
    private val onClick: (LocationResponseItem) -> Unit
) : RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocationResponseItem) {
            binding.locationName.text = item.displayName
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = suggestions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }
}
