package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onTaskClick: (TaskItem) -> Unit
) : ListAdapter<TaskItem, TaskAdapter.TaskVH>(DiffCallback()) {

    inner class TaskVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvStart : TextView = view.findViewById(R.id.tvTaskStart)
        val tvEnd   : TextView = view.findViewById(R.id.tvTaskEnd)
        val tvTitle : TextView = view.findViewById(R.id.tvTaskTitle)
        val tvNote  : TextView = view.findViewById(R.id.tvTaskNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskVH(v)
    }

    override fun onBindViewHolder(holder: TaskVH, position: Int) {
        val task = getItem(position)

        holder.tvStart.text = task.startTime
        holder.tvEnd.text   = task.endTime
        holder.tvTitle.text = task.title
        holder.tvNote.text  = task.note?.let { "Note: $it" } ?: ""

        holder.itemView.setOnClickListener { onTaskClick(task) }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(old: TaskItem, new: TaskItem) =
            old === new
        override fun areContentsTheSame(old: TaskItem, new: TaskItem) =
            old == new
    }
}
