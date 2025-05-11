package com.example.nomadnest.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import com.example.nomadnest.R
import com.example.nomadnest.data.models.Trip

class TripAdapter(
    private val onUpdateClick: (Trip) -> Unit,
    private val onDeleteClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private var tripList = listOf<Trip>()

    fun submitList(trips: List<Trip>) {
        tripList = trips
        notifyDataSetChanged()
    }

    inner class TripViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(trip: Trip) {
            view.findViewById<TextView>(R.id.destinationTextView).text = trip.destination
            view.findViewById<TextView>(R.id.dateTextView).text = trip.date
            view.findViewById<TextView>(R.id.budgetTextView).text = "Budget: ${trip.budget}"

            view.findViewById<View>(R.id.updateButton).setOnClickListener {
                onUpdateClick(trip)
            }

            view.findViewById<View>(R.id.deleteButton).setOnClickListener {
                onDeleteClick(trip)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(tripList[position])
    }

    override fun getItemCount() = tripList.size
}
