package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(
    private val items: List<FriendRequest>,
    private val onAccept: (FriendRequest)->Unit,
    private val onReject: (FriendRequest)->Unit
): RecyclerView.Adapter<FriendRequestAdapter.VH>() {

    inner class VH(view: View): RecyclerView.ViewHolder(view) {
        val tvEmail = view.findViewById<TextView>(R.id.tvRequestEmail)
        val btnAccept = view.findViewById<Button>(R.id.btnAccept)
        val btnReject = view.findViewById<Button>(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_request, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = items[position]
        holder.tvEmail.text = req.fromEmail
        holder.btnAccept.setOnClickListener { onAccept(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
    }

    override fun getItemCount() = items.size
}
