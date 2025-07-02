package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate

class TaskDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_TASK = "EXTRA_TASK"
        const val EXTRA_DATE_CONTEXT = "EXTRA_DATE_CONTEXT"
        const val EXTRA_PERMISSION = "EXTRA_PERMISSION"
        const val EXTRA_DELETE_ALL = "DELETE_ALL"
        const val EXTRA_DELETE_DATE = "DELETE_DATE"
    }

    private lateinit var tvTitle: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvRecurrence: TextView
    private lateinit var tvNote: TextView
    private lateinit var btnDeleteTask: Button

    private lateinit var permission: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        tvTitle       = findViewById(R.id.tvDetailTitle)
        tvDateTime    = findViewById(R.id.tvDetailDateTime)
        tvRecurrence  = findViewById(R.id.tvDetailRecurrence)
        tvNote        = findViewById(R.id.tvDetailNote)
        btnDeleteTask = findViewById(R.id.btnDeleteTask)

        val task       = intent.getSerializableExtra(EXTRA_TASK) as TaskItem
        val dateContext= intent.getStringExtra(EXTRA_DATE_CONTEXT)!!
        permission     = intent.getStringExtra(EXTRA_PERMISSION) ?: "edit"

        if (permission != "edit") {
            btnDeleteTask.visibility = View.GONE
        }

        tvTitle.text      = task.title
        tvDateTime.text   = buildString {
            append(dateContext)
            append("  ")
            append(task.startTime)
            append(" - ")
            append(task.endTime)
        }
        tvRecurrence.text = when {
            task.isDaily   -> "Repeats daily until ${task.recurrenceEndDate}"
            task.isWeekly  -> "Repeats weekly until ${task.recurrenceEndDate}"
            else           -> "No recurrence"
        }
        tvNote.text      = task.note ?: ""

        btnDeleteTask.setOnClickListener {
            Intent().apply {
                putExtra(EXTRA_DELETE_DATE, dateContext)
                putExtra(EXTRA_DELETE_ALL, false)
            }.also {
                setResult(Activity.RESULT_OK, it)
                finish()
            }
        }
    }
}
