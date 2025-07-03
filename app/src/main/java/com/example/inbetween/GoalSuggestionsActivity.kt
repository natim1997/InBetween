package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class GoalSuggestionsActivity : BaseActivity() {

    companion object {
        const val EXTRA_GOAL        = "EXTRA_GOAL"
        const val EXTRA_ALL_TASKS   = "EXTRA_ALL_TASKS"
        const val EXTRA_WEEK_START  = "EXTRA_WEEK_START"
        const val EXTRA_NEW_TASKS   = "EXTRA_NEW_TASKS"
        const val REQUEST_ADD_TASK  = HomeActivity.REQUEST_CHOOSE_ADD
    }

    private lateinit var rvSuggestions: RecyclerView
    private val suggestions   = mutableListOf<LocalDateTime>()
    private val acceptedTasks = mutableListOf<TaskItem>()
    private val timeFmt       = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_suggestions)

        rvSuggestions = findViewById(R.id.rvSuggestions)

        val goal = intent.getSerializableExtra(EXTRA_GOAL) as? GoalItem
        @Suppress("UNCHECKED_CAST")
        val existing = intent.getSerializableExtra(EXTRA_ALL_TASKS)
                as? ArrayList<TaskItem> ?: arrayListOf()
        val weekStart = intent.getStringExtra(EXTRA_WEEK_START)
            ?.let { LocalDate.parse(it) }
            ?: LocalDate.now()

        goal?.let {
            suggestions += computeSuggestions(it, existing, weekStart)
        }

        rvSuggestions.layoutManager = LinearLayoutManager(this)
        rvSuggestions.adapter = SuggestionsAdapter(
            items    = suggestions,
            onAccept = { dt ->
                acceptedTasks += dt.toTaskItemFromGoal(goal!!)
                suggestions.remove(dt)
                rvSuggestions.adapter?.notifyDataSetChanged()
                checkDone()
            },
            onReject = { dt ->
                suggestions.remove(dt)
                rvSuggestions.adapter?.notifyDataSetChanged()
                checkDone()
            },
            onOther  = { dt ->
                startActivityForResult(
                    Intent(this, AddTaskActivity::class.java)
                        .putExtra("SELECTED_DATE", dt.toLocalDate().toString())
                        .putExtra("SELECTED_TIME", dt.toLocalTime().format(timeFmt)),
                    REQUEST_ADD_TASK
                )
            }
        )
    }


    private fun computeSuggestions(
        goal: GoalItem,
        existingTasks: List<TaskItem>,
        weekStart: LocalDate
    ): List<LocalDateTime> {
        val result = mutableListOf<LocalDateTime>()
        val sessions = goal.sessionsPerWeek
        val workStart = LocalTime.of(9, 0)
        val workEnd   = LocalTime.of(18, 0)

        for (dayOffset in 0..6) {
            if (result.size >= sessions) break

            val date = weekStart.plusDays(dayOffset.toLong())
            var slotTime = workStart

            while (slotTime.plusMinutes(goal.durationMin.toLong()) <= workEnd) {
                val slotStart = date.atTime(slotTime)
                val slotEnd   = slotStart.plusMinutes(goal.durationMin.toLong())

                val conflict = existingTasks.any { t ->
                    if (t.date != date) return@any false
                    val tStart = LocalTime.parse(t.startTime, timeFmt)
                    val tEnd   = LocalTime.parse(t.endTime,   timeFmt)
                    !(slotEnd.toLocalTime() <= tStart || slotStart.toLocalTime() >= tEnd)
                }

                if (!conflict) {
                    result += slotStart
                    break
                }

                slotTime = slotTime.plusMinutes(goal.durationMin.toLong())
            }
        }

        return result.take(sessions)
    }

    private fun LocalDateTime.toTaskItemFromGoal(goal: GoalItem): TaskItem {
        val startStr = toLocalTime().format(timeFmt)
        val endStr   = plusMinutes(goal.durationMin.toLong())
            .toLocalTime().format(timeFmt)

        return TaskItem(
            title             = goal.title,
            date              = toLocalDate(),
            startTime         = startStr,
            endTime           = endStr,
            isDaily           = false,
            isWeekly          = false,
            recurrenceEndDate = null,
            excludedDates     = mutableListOf()
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_TASK
            && resultCode == Activity.RESULT_OK
            && data != null
        ) {
            (data.getSerializableExtra(AddTaskActivity.EXTRA_NEW_TASK) as? TaskItem)
                ?.let { acceptedTasks += it }
            checkDone()
        }
    }

    private fun checkDone() {
        if (suggestions.isEmpty()) {
            setResult(
                Activity.RESULT_OK,
                Intent().putExtra(EXTRA_NEW_TASKS, ArrayList(acceptedTasks))
            )
            finish()
        }
    }
}
