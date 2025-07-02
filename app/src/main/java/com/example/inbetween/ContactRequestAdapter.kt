package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ContactRequestAdapter(
    private val onAccept : (Request) -> Unit,
    private val onDecline: (Request) -> Unit
) : ListAdapter<Request, ContactRequestAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = getItem(position)
        holder.name.text     = req.fromName
        holder.accept.setOnClickListener  { onAccept(req) }
        holder.decline.setOnClickListener { onDecline(req) }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name    : TextView = view.findViewById(R.id.tvRequestName)
        val accept  : Button   = view.findViewById(R.id.btnAccept)
        val decline : Button   = view.findViewById(R.id.btnDecline)
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Request>() {
            override fun areItemsTheSame(a: Request, b: Request) =
                a.fromUid == b.fromUid

            override fun areContentsTheSame(a: Request, b: Request) =
                a == b
        }
    }
}
