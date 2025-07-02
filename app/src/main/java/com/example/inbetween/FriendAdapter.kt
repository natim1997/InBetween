package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FriendAdapter(
    private val onClick:   (Friend) -> Unit,
    private val onDelete:  (Friend) -> Unit
) : ListAdapter<Friend, FriendAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val friend = getItem(position)
        holder.tvName.text = friend.name
        holder.itemView.setOnClickListener  { onClick(friend) }
        holder.btnDelete.setOnClickListener { onDelete(friend) }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName   : TextView    = view.findViewById(R.id.tvFriendName)
        val btnDelete: ImageButton = view.findViewById(R.id.btnRemoveFriend)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Friend>() {
            override fun areItemsTheSame(old: Friend, new: Friend) =
                old.uid == new.uid
            override fun areContentsTheSame(old: Friend, new: Friend) =
                old == new
        }
    }
}
