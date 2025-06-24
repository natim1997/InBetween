package com.example.inbetween

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeActivity : BaseActivity() {

    companion object {
        private const val REQUEST_ADD_TASK = 1001
    }

    private lateinit var rvDates     : RecyclerView
    private lateinit var rvTasks     : RecyclerView
    private lateinit var fabAddTask  : FloatingActionButton
    private lateinit var btnPrevWeek : ImageButton
    private lateinit var btnNextWeek : ImageButton
    private lateinit var tvWeekTitle : TextView

    private val allTasks = mutableListOf<TaskItem>()
    private var selectedDate : LocalDate = LocalDate.now()
    private var weekStart     : LocalDate = selectedDate.with(DayOfWeek.SUNDAY)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvDates    = findViewById(R.id.rvDates)
        rvTasks    = findViewById(R.id.rvTasks)
        fabAddTask = findViewById(R.id.fabAddTask)
        btnPrevWeek= findViewById(R.id.btnPrevWeek)
        btnNextWeek= findViewById(R.id.btnNextWeek)
        tvWeekTitle= findViewById(R.id.tvWeekLabel)

        rvDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = DateAdapter(generateWeekDates(weekStart)) { date ->
            selectedDate = date
            updateTasks()
        }

        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = TaskAdapter()

        fabAddTask.setOnClickListener {
            Intent(this, AddTaskActivity::class.java).also {
                it.putExtra("SELECTED_DATE", selectedDate.toString())
                startActivityForResult(it, REQUEST_ADD_TASK)
            }
        }

        btnPrevWeek.setOnClickListener {
            weekStart = weekStart.minusWeeks(1)
            refreshWeek()
        }
        btnNextWeek.setOnClickListener {
            weekStart = weekStart.plusWeeks(1)
            refreshWeek()
        }

        refreshWeek()
    }

    private fun refreshWeek() {
        val dates = generateWeekDates(weekStart)
        (rvDates.adapter as DateAdapter).updateDates(dates)

        // keep selection within current week
        if (selectedDate !in dates) selectedDate = weekStart

        updateTasks()

        val monthName = weekStart.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        tvWeekTitle.text = "Week of ${weekStart.dayOfMonth} $monthName"
    }

    private fun updateTasks() {
        val tasksForDay = allTasks.filter { task ->
            when {
                task.date == selectedDate -> true
                task.isDaily &&
                        !selectedDate.isBefore(task.date) &&
                        (task.recurrenceEndDate == null || !selectedDate.isAfter(task.recurrenceEndDate)) ->
                    true
                task.isWeekly &&
                        !selectedDate.isBefore(task.date) &&
                        (task.recurrenceEndDate == null || !selectedDate.isAfter(task.recurrenceEndDate)) &&
                        selectedDate.dayOfWeek == task.date.dayOfWeek ->
                    true
                else -> false
            }
        }.sortedBy { LocalTime.parse(it.startTime, timeFormatter) }

        (rvTasks.adapter as TaskAdapter).submitList(tasksForDay)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_TASK && resultCode == RESULT_OK) {
            (data?.getSerializableExtra("NEW_TASK") as? TaskItem)?.let {
                allTasks.add(it)
                refreshWeek()
            }
        }
    }

    private fun generateWeekDates(center: LocalDate): List<LocalDate> {
        val sunday = center.with(DayOfWeek.SUNDAY)
        return (0L..6L).map { sunday.plusDays(it) }
    }
}
