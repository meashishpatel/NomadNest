package com.example.nomadnest

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater

class TripAdapter(private val trips: List<Trip>) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val destination = itemView.findViewById<TextView>(R.id.destinationTextView)
        val date = itemView.findViewById<TextView>(R.id.dateTextView)
        val budget = itemView.findViewById<TextView>(R.id.budgetTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.destination.text = trip.destination
        holder.date.text = trip.date
        holder.budget.text = trip.budget
    }

    override fun getItemCount(): Int = trips.size
}
