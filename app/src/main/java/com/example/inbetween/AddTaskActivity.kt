package com.example.inbetween

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTitle         : TextInputEditText
    private lateinit var etStart         : TextInputEditText
    private lateinit var etEnd           : TextInputEditText
    private lateinit var etDate          : TextInputEditText
    private lateinit var rgRepeat        : RadioGroup
    private lateinit var radioNever      : RadioButton
    private lateinit var radioDaily      : RadioButton
    private lateinit var radioWeekly     : RadioButton
    private lateinit var lytRecEnd       : View
    private lateinit var etRecurrenceEnd : TextInputEditText
    private lateinit var etNote          : TextInputEditText
    private lateinit var btnSaveTask     : Button

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTitle          = findViewById(R.id.etTitle)
        etStart          = findViewById(R.id.etStartTime)
        etEnd            = findViewById(R.id.etEndTime)
        etDate           = findViewById(R.id.etDate)
        rgRepeat         = findViewById(R.id.rgRepeat)
        radioNever       = findViewById(R.id.radioNever)
        radioDaily       = findViewById(R.id.radioDaily)
        radioWeekly      = findViewById(R.id.radioWeekly)
        lytRecEnd        = findViewById(R.id.lytRecEnd)
        etRecurrenceEnd  = findViewById(R.id.etRecurrenceEnd)
        etNote           = findViewById(R.id.etNote)
        btnSaveTask      = findViewById(R.id.btnSaveTask)

        // time/date pickers
        etStart.setOnClickListener { pickTime { etStart.setText(it) } }
        etEnd.setOnClickListener   { pickTime { etEnd.setText(it) } }
        etDate.setOnClickListener  { pickDate { etDate.setText(it) } }
        etRecurrenceEnd.setOnClickListener { pickDate { etRecurrenceEnd.setText(it) } }

        // show “repeat until” only when daily or weekly
        rgRepeat.setOnCheckedChangeListener { _, checkedId ->
            lytRecEnd.visibility =
                if (checkedId == R.id.radioDaily || checkedId == R.id.radioWeekly)
                    View.VISIBLE
                else
                    View.GONE
        }

        btnSaveTask.setOnClickListener {
            // validate start ≤ end
            val s = etStart.text.toString()
            val e = etEnd.text.toString()
            if (s.isNotBlank() && e.isNotBlank()) {
                val t1 = LocalTime.parse(s, timeFormatter)
                val t2 = LocalTime.parse(e, timeFormatter)
                if (t1.isAfter(t2)) {
                    etStart.error = "Must be before end"
                    return@setOnClickListener
                }
            }

            val dateVal = LocalDate.parse(etDate.text.toString())
            val recEnd = if (radioDaily.isChecked || radioWeekly.isChecked)
                LocalDate.parse(etRecurrenceEnd.text.toString())
            else
                null

            val task = TaskItem(
                title               = etTitle.text.toString(),
                date                = dateVal,
                startTime           = s,
                endTime             = e,
                note                = etNote.text.toString().takeIf { it.isNotBlank() },
                isWeekly            = radioWeekly.isChecked,
                isDaily             = radioDaily.isChecked,
                recurrenceEndDate   = recEnd
            )

            Intent().also {
                it.putExtra("NEW_TASK", task as Serializable)
                setResult(RESULT_OK, it)
            }
            finish()
        }
    }

    private fun pickTime(onPicked: (String) -> Unit) {
        val now = LocalTime.now()
        TimePickerDialog(this, { _, h, m ->
            onPicked(LocalTime.of(h, m).format(timeFormatter))
        }, now.hour, now.minute, true).show()
    }

    private fun pickDate(onPicked: (String) -> Unit) {
        val today = LocalDate.now()
        DatePickerDialog(
            this,
            { _, y, mo, d -> onPicked(LocalDate.of(y, mo + 1, d).toString()) },
            today.year, today.monthValue - 1, today.dayOfMonth
        ).show()
    }
}
