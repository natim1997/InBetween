package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeActivity : BaseActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb  = FirebaseFirestore.getInstance()

    private lateinit var targetUserId: String
    private lateinit var targetPermission: String

    private var lastViewedTask: TaskItem? = null

    companion object {
        const val REQUEST_CHOOSE_ADD       = 3001
        const val REQUEST_GOAL_SUGGESTIONS = 2002
        const val REQUEST_VIEW_TASK        = 3005

        const val EXTRA_VIEW_USER   = "EXTRA_VIEW_USER"
        const val EXTRA_PERMISSION  = "EXTRA_PERMISSION"
        const val EXTRA_NEW_TASK    = "NEW_TASK"
        const val EXTRA_GOAL        = "EXTRA_GOAL"
    }

    private val allTasks     = mutableListOf<TaskItem>()
    private var selectedDate = LocalDate.now()
    private var weekStart    = selectedDate.with(DayOfWeek.SUNDAY)
    private val timeFmt      = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var adapter: TaskAdapter

    private lateinit var rvDates     : RecyclerView
    private lateinit var rvTasks     : RecyclerView
    private lateinit var fabAddTask  : FloatingActionButton
    private lateinit var fabContacts : FloatingActionButton
    private lateinit var btnPrevWeek : ImageButton
    private lateinit var btnNextWeek : ImageButton
    private lateinit var tvWeekLabel : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        targetUserId     = intent.getStringExtra(EXTRA_VIEW_USER)
            ?: firebaseAuth.currentUser!!.uid
        targetPermission = intent.getStringExtra(EXTRA_PERMISSION)
            ?: "edit"

        rvDates     = findViewById(R.id.rvDates)
        rvTasks     = findViewById(R.id.rvTasks)
        fabAddTask  = findViewById(R.id.fabAddTask)
        fabContacts = findViewById(R.id.fabContacts)
        btnPrevWeek = findViewById(R.id.btnPrevWeek)
        btnNextWeek = findViewById(R.id.btnNextWeek)
        tvWeekLabel = findViewById(R.id.tvWeekLabel)

        rvDates.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = DateAdapter(generateWeekDates(weekStart)) { date ->
            selectedDate = date
            refreshWeek()
        }

        adapter = TaskAdapter { task ->
            lastViewedTask = task
            Intent(this, TaskDetailActivity::class.java).apply {
                putExtra(TaskDetailActivity.EXTRA_TASK, task)
                putExtra(TaskDetailActivity.EXTRA_DATE_CONTEXT, selectedDate.toString())
                putExtra(EXTRA_PERMISSION, targetPermission)
            }.also {
                startActivityForResult(it, REQUEST_VIEW_TASK)
            }
        }
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter      = adapter

        fabAddTask.setOnClickListener {
            if (targetPermission == "edit") {
                startActivityForResult(
                    Intent(this, ChooseActionActivity::class.java),
                    REQUEST_CHOOSE_ADD
                )
            }
        }
        fabContacts.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }
        btnPrevWeek.setOnClickListener {
            weekStart = weekStart.minusWeeks(1); refreshWeek()
        }
        btnNextWeek.setOnClickListener {
            weekStart = weekStart.plusWeeks(1); refreshWeek()
        }
        if (targetPermission != "edit") {
            fabAddTask.visibility = View.GONE
        }

        loadTasksFromFirestore()
        refreshWeek()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_CHOOSE_ADD -> {
                (data.getSerializableExtra(EXTRA_NEW_TASK) as? TaskItem)?.let {
                    saveTaskToFirestore(it)
                    return
                }
                (data.getSerializableExtra(EXTRA_GOAL) as? GoalItem)?.let { goal ->
                    Intent(this, GoalSuggestionsActivity::class.java).apply {
                        putExtra(GoalSuggestionsActivity.EXTRA_GOAL, goal)
                        putExtra(
                            GoalSuggestionsActivity.EXTRA_ALL_TASKS,
                            ArrayList(allTasks)
                        )
                        putExtra(
                            GoalSuggestionsActivity.EXTRA_WEEK_START,
                            weekStart.toString()
                        )
                    }.also {
                        startActivityForResult(it, REQUEST_GOAL_SUGGESTIONS)
                    }
                }
            }

            REQUEST_GOAL_SUGGESTIONS -> {
                @Suppress("UNCHECKED_CAST")
                val picks = data.getSerializableExtra(
                    GoalSuggestionsActivity.EXTRA_NEW_TASKS
                ) as? ArrayList<TaskItem> ?: arrayListOf()
                picks.forEach { saveTaskToFirestore(it) }
            }

            REQUEST_VIEW_TASK -> {
            }
        }
    }

    private fun saveTaskToFirestore(task: TaskItem) {
        val col = firestoreDb
            .collection("users")
            .document(targetUserId)
            .collection("tasks")
        val docId = "${task.title}_${task.date}"
        col.document(docId)
            .set(task.toMap())
    }

    private fun loadTasksFromFirestore() {
        firestoreDb.collection("users")
            .document(targetUserId)
            .collection("tasks")
            .addSnapshotListener { snaps, error ->
                if (error != null || snaps == null) return@addSnapshotListener
                allTasks.clear()
                for (doc in snaps.documents) {
                    val dateStr = doc.getString("date")?.takeIf { it.isNotBlank() } ?: continue
                    val title   = doc.getString("title") ?: continue
                    val start   = doc.getString("startTime")
                        ?.takeIf { it.isNotBlank() } ?: continue
                    val end     = doc.getString("endTime")
                        ?.takeIf { it.isNotBlank() } ?: continue
                    val isDaily = doc.getBoolean("isDaily") ?: false
                    val isWeekly= doc.getBoolean("isWeekly") ?: false
                    val recEnd  = doc.getString("recurrenceEndDate")
                        ?.takeIf { it.isNotBlank() }
                        ?.let { LocalDate.parse(it) }
                    val excluded = (doc.get("excludedDates") as? List<String> ?: emptyList())
                        .mapNotNull { it.takeIf { s -> s.isNotBlank() }?.let { d -> LocalDate.parse(d) } }
                        .toMutableList()

                    allTasks += TaskItem(
                        title             = title,
                        date              = LocalDate.parse(dateStr),
                        startTime         = start,
                        endTime           = end,
                        isDaily           = isDaily,
                        isWeekly          = isWeekly,
                        recurrenceEndDate = recEnd,
                        excludedDates     = excluded
                    )
                }
                refreshWeek()
            }
    }

    private fun refreshWeek() {
        val dates = generateWeekDates(weekStart)
        (rvDates.adapter as DateAdapter).updateDates(dates)
        if (selectedDate !in dates) selectedDate = weekStart
        updateTasks()
        val monthEnglish =
            weekStart.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        tvWeekLabel.text = "Week of ${weekStart.dayOfMonth} $monthEnglish"
    }

    private fun updateTasks() {
        val list = allTasks.filter { t ->
            if (selectedDate in t.excludedDates) return@filter false
            when {
                t.date == selectedDate -> true
                t.isDaily && !selectedDate.isBefore(t.date)
                        && (t.recurrenceEndDate == null
                        || !selectedDate.isAfter(t.recurrenceEndDate)) -> true
                t.isWeekly && !selectedDate.isBefore(t.date)
                        && (t.recurrenceEndDate == null
                        || !selectedDate.isAfter(t.recurrenceEndDate))
                        && selectedDate.dayOfWeek == t.date.dayOfWeek -> true
                else -> false
            }
        }.sortedBy { LocalTime.parse(it.startTime, timeFmt) }

        adapter.submitList(list)
    }

    private fun generateWeekDates(center: LocalDate) =
        (0L..6L).map { center.with(DayOfWeek.SUNDAY).plusDays(it) }

    private fun TaskItem.toMap(): Map<String, Any> {
        val m = mutableMapOf<String, Any>(
            "title"     to title,
            "date"      to date.toString(),
            "startTime" to startTime,
            "endTime"   to endTime,
            "isDaily"   to isDaily,
            "isWeekly"  to isWeekly
        )
        recurrenceEndDate?.let { m["recurrenceEndDate"] = it.toString() }
        if (excludedDates.isNotEmpty()) {
            m["excludedDates"] = excludedDates.map { it.toString() }
        }
        return m
    }
}
