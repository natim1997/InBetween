package com.example.inbetween

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddTaskActivity : BaseActivity() {

    companion object {
        const val EXTRA_NEW_TASK = "NEW_TASK"
    }

    private lateinit var etTitle: TextInputEditText
    private lateinit var etStartTime: TextInputEditText
    private lateinit var etEndTime: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etNote: TextInputEditText
    private lateinit var rgRepeat: RadioGroup
    private lateinit var radioNever: RadioButton
    private lateinit var radioDaily: RadioButton
    private lateinit var radioWeekly: RadioButton
    private lateinit var lytRecEnd: LinearLayout
    private lateinit var etRecurrenceEnd: TextInputEditText
    private lateinit var swPermanent: SwitchMaterial
    private lateinit var btnSave: Button

    private var selectedDate: LocalDate = LocalDate.now()
    private var recurrenceEnd: LocalDate? = null

    private val dateFmt = DateTimeFormatter.ISO_DATE
    private val displayDateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTitle          = findViewById(R.id.etTitle)
        etStartTime      = findViewById(R.id.etStartTime)
        etEndTime        = findViewById(R.id.etEndTime)
        etDate           = findViewById(R.id.etDate)
        etNote           = findViewById(R.id.etNote)
        rgRepeat         = findViewById(R.id.rgRepeat)
        radioNever       = findViewById(R.id.radioNever)
        radioDaily       = findViewById(R.id.radioDaily)
        radioWeekly      = findViewById(R.id.radioWeekly)
        lytRecEnd        = findViewById(R.id.lytRecEnd)
        etRecurrenceEnd  = findViewById(R.id.etRecurrenceEnd)
        swPermanent      = findViewById(R.id.swPermanent)
        btnSave          = findViewById(R.id.btnSaveTask)

        intent.getStringExtra("SELECTED_DATE")?.let {
            selectedDate = LocalDate.parse(it, dateFmt)
        }
        etDate.setText(displayDateFmt.format(selectedDate))

        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedDate = LocalDate.of(y, m + 1, d)
                    etDate.setText(displayDateFmt.format(selectedDate))
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            ).show()
        }

        etStartTime.setOnClickListener {
            val now = java.time.LocalTime.now()
            TimePickerDialog(
                this,
                { _, h, min ->
                    etStartTime.setText(String.format("%02d:%02d", h, min))
                },
                now.hour, now.minute, true
            ).show()
        }

        etEndTime.setOnClickListener {
            val now = java.time.LocalTime.now()
            TimePickerDialog(
                this,
                { _, h, min ->
                    etEndTime.setText(String.format("%02d:%02d", h, min))
                },
                now.hour, now.minute, true
            ).show()
        }

        rgRepeat.setOnCheckedChangeListener { _, checkedId ->
            lytRecEnd.visibility =
                if (checkedId == R.id.radioDaily || checkedId == R.id.radioWeekly)
                    android.view.View.VISIBLE
                else
                    android.view.View.GONE
        }

        etRecurrenceEnd.setOnClickListener {
            val base = recurrenceEnd ?: selectedDate
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    recurrenceEnd = LocalDate.of(y, m + 1, d)
                    etRecurrenceEnd.setText(displayDateFmt.format(recurrenceEnd))
                },
                base.year,
                base.monthValue - 1,
                base.dayOfMonth
            ).show()
        }

        btnSave.setOnClickListener {
            val title      = etTitle.text.toString().ifBlank { "Untitled" }
            val startTime  = etStartTime.text.toString().ifBlank { "09:00" }
            val endTime    = etEndTime.text.toString().ifBlank { "10:00" }
            val note       = etNote.text.toString()
            val isDaily    = radioDaily.isChecked
            val isWeekly   = radioWeekly.isChecked
            val recEndDate = if (isDaily || isWeekly) recurrenceEnd else null
            val isPermanent = swPermanent.isChecked

            val task = TaskItem(
                title             = title,
                date              = selectedDate,
                startTime         = startTime,
                endTime           = endTime,
                isDaily           = isDaily,
                isWeekly          = isWeekly,
                recurrenceEndDate = recEndDate,
                excludedDates     = mutableListOf(),
                isPermanent       = isPermanent,
                note              = note
            )

            Intent().putExtra(EXTRA_NEW_TASK, task).also {
                setResult(Activity.RESULT_OK, it)
                finish()
            }
        }
    }
}
