package com.example.inbetween

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.FirebaseFirestore


class AddEditGoalActivity : BaseActivity() {

    companion object {
        const val EXTRA_GOAL = "EXTRA_GOAL"
    }

    private lateinit var etGoalName : EditText
    private lateinit var npSessions : NumberPicker
    private lateinit var npDuration : NumberPicker
    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate  : Button
    private lateinit var btnSave     : Button

    private var startDate: LocalDate = LocalDate.now()
    private var endDate  : LocalDate = startDate.plusWeeks(4)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var editingGoal: GoalItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_goal)

        etGoalName   = findViewById(R.id.etGoalName)
        npSessions   = findViewById(R.id.npSessions)
        npDuration   = findViewById(R.id.npDuration)
        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate   = findViewById(R.id.btnEndDate)
        btnSave      = findViewById(R.id.btnSaveGoal)

        npSessions.minValue = 1; npSessions.maxValue = 7
        npDuration.minValue = 10; npDuration.maxValue = 180

        editingGoal = intent.getSerializableExtra(EXTRA_GOAL) as? GoalItem
        editingGoal?.let { g ->
            etGoalName.setText(g.title)
            npSessions.value = g.sessionsPerWeek
            npDuration.value = g.durationMin
            startDate = g.startDate
            endDate   = g.endDate
        }
        updateDateButtons()

        btnStartDate.setOnClickListener {
            showPicker(startDate) { d -> startDate = d; updateDateButtons() }
        }
        btnEndDate.setOnClickListener {
            showPicker(endDate) { d -> endDate = d; updateDateButtons() }
        }

        btnSave.setOnClickListener {
            val goal = GoalItem(
                id               = editingGoal?.id ?: GoalItem.nextId(),
                title            = etGoalName.text.toString(),
                sessionsPerWeek  = npSessions.value,
                durationMin      = npDuration.value,
                startDate        = startDate,
                endDate          = endDate
            )
            Intent().putExtra(EXTRA_GOAL, goal).also {
                setResult(RESULT_OK, it)
                finish()
            }
        }
    }

    private fun updateDateButtons() {
        btnStartDate.text = startDate.format(dateFmt)
        btnEndDate.text   = endDate.format(dateFmt)
    }

    private fun showPicker(current: LocalDate, onPicked: (LocalDate) -> Unit) {
        DatePickerDialog(
            this,
            { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) },
            current.year, current.monthValue - 1, current.dayOfMonth
        ).show()
    }
}
