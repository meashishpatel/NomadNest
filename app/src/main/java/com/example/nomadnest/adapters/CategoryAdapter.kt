package com.example.nomadnest.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.nomadnest.R
import com.example.nomadnest.data.models.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val icon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val text: TextView = itemView.findViewById(R.id.categoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.categories_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.text.text = category.name
        holder.icon.setImageResource(category.iconRes)

        //  Change selection appearance
        holder.cardView.isSelected = category.isSelected
        holder.icon.setColorFilter(
            if (category.isSelected) holder.itemView.context.getColor(R.color.blue)
            else holder.itemView.context.getColor(R.color.gray)
        )
        holder.text.setTextColor(
            if (category.isSelected) holder.itemView.context.getColor(R.color.blue)
            else holder.itemView.context.getColor(R.color.gray)
        )

        //  Handle Category Click
        holder.cardView.setOnClickListener {
            categories.forEach { it.isSelected = false } // Unselect all
            category.isSelected = true // Select the clicked one
            notifyDataSetChanged()
            onClick(category) // Trigger the callback to fetch new images
        }
    }

    override fun getItemCount(): Int = categories.size
}
