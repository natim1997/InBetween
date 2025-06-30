package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalsAdapter(
    private val items: List<GoalItem>,
    private val onEdit: (GoalItem) -> Unit,
    private val onDelete: (GoalItem) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalVH>() {

    inner class GoalVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGoalName)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditGoal)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalVH(v)
    }

    override fun onBindViewHolder(holder: GoalVH, position: Int) {
        val goal = items[position]
        holder.tvName.text = goal.title
        holder.btnEdit.setOnClickListener { onEdit(goal) }
        holder.btnDelete.setOnClickListener { onDelete(goal) }
    }

    override fun getItemCount(): Int = items.size
}
