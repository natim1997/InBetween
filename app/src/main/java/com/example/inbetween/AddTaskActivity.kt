package com.example.inbetween

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTitle     : TextInputEditText
    private lateinit var etStart     : TextInputEditText
    private lateinit var etEnd       : TextInputEditText
    private lateinit var etDate      : TextInputEditText
    private lateinit var switchWeekly: SwitchMaterial
    private lateinit var etRecEnd    : TextInputEditText
    private lateinit var lytRecEnd   : View
    private lateinit var etNote      : TextInputEditText
    private lateinit var etBring     : TextInputEditText
    private lateinit var etReminder  : TextInputEditText
    private lateinit var etTravel    : TextInputEditText
    private lateinit var btnSave     : Button

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTitle      = findViewById(R.id.etTitle)
        etStart      = findViewById(R.id.etStartTime)
        etEnd        = findViewById(R.id.etEndTime)
        etDate       = findViewById(R.id.etDate)
        switchWeekly = findViewById(R.id.switchWeekly)
        etRecEnd     = findViewById(R.id.etRecurrenceEnd)
        lytRecEnd    = findViewById(R.id.lytRecurrenceEnd)
        etNote       = findViewById(R.id.etNote)
        etBring      = findViewById(R.id.etBring)
        etReminder   = findViewById(R.id.etReminder)
        etTravel     = findViewById(R.id.etTravel)
        btnSave      = findViewById(R.id.btnSaveTask)

        // pickers
        etStart .setOnClickListener { pickTime { etStart.setText(it) } }
        etEnd   .setOnClickListener { pickTime { etEnd.setText(it) } }
        etDate  .setOnClickListener { pickDate { etDate.setText(it) } }
        etRecEnd.setOnClickListener { pickDate { etRecEnd.setText(it) } }

        switchWeekly.setOnCheckedChangeListener { _, on ->
            lytRecEnd.visibility = if (on) View.VISIBLE else View.GONE
        }

        btnSave.setOnClickListener {
            val dateVal    = LocalDate.parse(etDate.text.toString())
            val recEndDate = etRecEnd.text.toString()
                .takeIf { switchWeekly.isChecked }
                ?.let { LocalDate.parse(it) }

            val task = TaskItem(
                title               = etTitle.text.toString(),
                date                = dateVal,
                startTime           = etStart.text.toString(),
                endTime             = etEnd.text.toString(),
                note                = etNote.text.toString().takeIf { it.isNotBlank() },
                bring               = etBring.text.toString().takeIf { it.isNotBlank() },
                remindBeforeMinutes = etReminder.text.toString().toIntOrNull(),
                travelMinutes       = etTravel.text.toString().toIntOrNull(),
                isWeekly            = switchWeekly.isChecked,
                recurrenceEndDate   = recEndDate
            )

            val data = Intent().apply {
                putExtra("NEW_TASK", task as Serializable)
            }
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun pickTime(onPicked: (String) -> Unit) {
        val now = LocalTime.now()
        TimePickerDialog(this,{_,h,m ->
            onPicked(LocalTime.of(h,m).format(timeFormatter))
        }, now.hour, now.minute, true).show()
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val today = LocalDate.now()
        DatePickerDialog(this,
            {_, y, mo, d -> onPicked(LocalDate.of(y, mo+1, d).toString()) },
            today.year, today.monthValue-1, today.dayOfMonth
        ).show()
    }
}
