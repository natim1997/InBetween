package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.time.LocalDate

class TaskDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_TASK = "EXTRA_TASK"
        const val EXTRA_DATE_CONTEXT = "EXTRA_DATE_CONTEXT"
        const val EXTRA_PERMISSION = "EXTRA_PERMISSION"
        const val EXTRA_DELETE_ALL = "DELETE_ALL"
        const val EXTRA_DELETE_DATE = "DELETE_DATE"
    }

    private lateinit var task: TaskItem
    private lateinit var dateContext: String

    private lateinit var tvTitle: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvRecurrence: TextView
    private lateinit var tvNote: TextView
    private lateinit var btnDeleteTask: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        task = intent.getSerializableExtra(EXTRA_TASK) as TaskItem
        dateContext = intent.getStringExtra(EXTRA_DATE_CONTEXT)!!

        tvTitle       = findViewById(R.id.tvDetailTitle)
        tvDateTime    = findViewById(R.id.tvDetailDateTime)
        tvRecurrence  = findViewById(R.id.tvDetailRecurrence)
        tvNote        = findViewById(R.id.tvDetailNote)
        btnDeleteTask = findViewById(R.id.btnDeleteTask)

        val permission = intent.getStringExtra(EXTRA_PERMISSION) ?: "edit"
        if (permission != "edit") {
            btnDeleteTask.visibility = View.GONE
        }

        tvTitle.text = task.title
        tvDateTime.text = "$dateContext  ${task.startTime} - ${task.endTime}"
        tvRecurrence.text = when {
            task.isDaily  -> "Repeats daily until ${task.recurrenceEndDate}"
            task.isWeekly -> "Repeats weekly until ${task.recurrenceEndDate}"
            else          -> "No recurrence"
        }
        tvNote.text = task.note ?: ""

        btnDeleteTask.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Delete task?")
                .setMessage("Remove only this occurrence or delete all occurrences?")
                .setPositiveButton("Only this date") { _, _ -> sendResult(false) }
                .setNegativeButton("All occurrences") { _, _ -> sendResult(true) }
                .setNeutralButton("Cancel", null)
                .create()

            dialog.show()
            // now shift both buttons to start
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).let { btn ->
                (btn.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).let { btn ->
                (btn.layoutParams as LinearLayout.LayoutParams).gravity = Gravity.START
            }
        }
    }

    private fun sendResult(deleteAll: Boolean) {
        Intent().apply {
            putExtra(EXTRA_TASK, task)
            putExtra(EXTRA_DELETE_DATE, dateContext)
            putExtra(EXTRA_DELETE_ALL, deleteAll)
        }.also {
            setResult(Activity.RESULT_OK, it)
            finish()
        }
    }
}
