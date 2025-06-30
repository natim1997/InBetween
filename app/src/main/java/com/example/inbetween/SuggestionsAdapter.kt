package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime

class SuggestionsAdapter(
    private val items    : List<LocalDateTime>,
    private val onAccept : (LocalDateTime) -> Unit,
    private val onReject : (LocalDateTime) -> Unit,
    private val onOther  : (LocalDateTime) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvSuggestion: TextView = view.findViewById(R.id.tvSuggestion)
        val btnAccept   : Button   = view.findViewById(R.id.btnAccept)
        val btnReject   : Button   = view.findViewById(R.id.btnReject)
        val btnOther    : Button   = view.findViewById(R.id.btnOther)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val dt = items[position]
        holder.tvSuggestion.text = dt.format(
            java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d HH:mm")
        )
        holder.btnAccept.setOnClickListener { onAccept(dt) }
        holder.btnReject.setOnClickListener { onReject(dt) }
        holder.btnOther.setOnClickListener { onOther(dt) }
    }
}
