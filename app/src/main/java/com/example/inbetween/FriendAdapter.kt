package com.example.inbetween

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(
    private val items: List<Friend>
): RecyclerView.Adapter<FriendAdapter.VH>() {

    inner class VH(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
    ) {
        val tv = itemView.findViewById<TextView>(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tv.text = items[position].email
    }
}
