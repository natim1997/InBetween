package com.example.inbetween

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class DateAdapter(
    private var items: List<LocalDate>,
    private val onClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateVH>() {

    private var selectedPos = 0

    inner class DateVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayShort   : TextView = view.findViewById(R.id.tvDayShort)
        val tvDateNumber : TextView = view.findViewById(R.id.tvDateNumber)

        init {
            view.setOnClickListener {
                val old = selectedPos
                selectedPos = adapterPosition
                notifyItemChanged(old)
                notifyItemChanged(selectedPos)
                onClick(items[selectedPos])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date, parent, false)
        return DateVH(v)
    }

    override fun onBindViewHolder(holder: DateVH, position: Int) {
        val date = items[position]
        holder.tvDayShort.text = date
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            .uppercase(Locale.getDefault())
        holder.tvDateNumber.text = date.dayOfMonth.toString()

        holder.itemView.setBackgroundResource(
            if (position == selectedPos)
                R.drawable.bg_date_cell_selected
            else
                R.drawable.bg_date_cell
        )
    }

    override fun getItemCount() = items.size

    /**
     * תוקף את רשימת התאריכים ומעדכן בחירה לפי התאריך הנוכחי (optional)
     */
    fun updateDates(newDates: List<LocalDate>) {
        items = newDates
        // מחזיר את המיקום שבעצם נבחר
        selectedPos = newDates.indexOfFirst { it == items.getOrNull(selectedPos) }
            .takeIf { it >= 0 } ?: 0
        notifyDataSetChanged()
    }
}
