package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.time.LocalDate

class TaskDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_TASK         = "TASK_ITEM"
        const val EXTRA_DATE_CONTEXT = "DATE_CONTEXT"
        const val EXTRA_DELETE_ALL   = "DELETE_ALL"
        const val EXTRA_DELETE_DATE  = "DELETE_DATE"
    }

    private lateinit var task: TaskItem
    private lateinit var tvTitle: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvRecurrence: TextView
    private lateinit var tvNote: TextView
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        task = intent.getSerializableExtra(EXTRA_TASK) as TaskItem

        tvTitle      = findViewById(R.id.tvDetailTitle)
        tvDateTime   = findViewById(R.id.tvDetailDateTime)
        tvRecurrence = findViewById(R.id.tvDetailRecurrence)
        tvNote       = findViewById(R.id.tvDetailNote)
        btnDelete    = findViewById(R.id.btnDeleteTask)

        tvTitle.text = task.title
        tvDateTime.text = "${task.date} | ${task.startTime} – ${task.endTime}"
        tvRecurrence.text = when {
            task.isDaily  -> "Recurrence: Daily"
            task.isWeekly -> "Recurrence: Weekly"
            else          -> "Recurrence: None"
        }
        tvNote.text = ""

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setItems(arrayOf(
                    "Delete All Occurrences",
                    "Delete This Occurrence"
                )) { _, which ->
                    val data = Intent()
                    if (which == 0) {
                        data.putExtra(EXTRA_DELETE_ALL, true)
                    } else {
                        val dateStr = intent.getStringExtra(EXTRA_DATE_CONTEXT)
                        data.putExtra(EXTRA_DELETE_DATE, dateStr)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
                .show()
        }
    }
}
