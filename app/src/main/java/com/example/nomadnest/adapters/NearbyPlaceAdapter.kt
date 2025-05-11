package com.example.nomadnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nomadnest.R
import com.example.nomadnest.data.models.NearbyPlace

class NearbyPlaceAdapter(private val places: List<NearbyPlace>) :
    RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.placeImage)
        val title: TextView = itemView.findViewById(R.id.placeTitle)
        val distance: TextView = itemView.findViewById(R.id.placeDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.title.text = place.title
        holder.distance.text = "${place.distance} meters"
        if (!place.thumbnailUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(place.thumbnailUrl)
                .placeholder(R.drawable.location_ic)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.location_ic)
        }
    }

    override fun getItemCount() = places.size
}
