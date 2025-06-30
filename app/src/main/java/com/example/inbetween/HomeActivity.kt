package com.example.inbetween

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

class HomeActivity : BaseActivity() {
    private val auth = FirebaseAuth.getInstance()
    private var lastViewedTask: TaskItem? = null

    companion object {
        const val REQUEST_CHOOSE_ADD       = 3001
        const val REQUEST_GOAL_SUGGESTIONS = 3004
        const val REQUEST_VIEW_TASK        = 3005

        const val EXTRA_NEW_TASK    = "NEW_TASK"
        const val EXTRA_NEW_TASKS   = "EXTRA_NEW_TASKS"
        const val EXTRA_GOAL        = "EXTRA_GOAL"
        const val EXTRA_ALL_TASKS   = "EXTRA_ALL_TASKS"
        const val EXTRA_WEEK_START  = "EXTRA_WEEK_START"
        const val EXTRA_DELETE_ALL  = "DELETE_ALL"
        const val EXTRA_DELETE_DATE = "DELETE_DATE"
    }

    private val allTasks     = mutableListOf<TaskItem>()
    private var selectedDate = LocalDate.now()
    private var weekStart    = selectedDate.with(DayOfWeek.SUNDAY)
    private val timeFmt      = DateTimeFormatter.ofPattern("HH:mm")
    private lateinit var adapter: TaskAdapter

    private lateinit var rvDates: RecyclerView
    private lateinit var rvTasks: RecyclerView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var fabContacts: FloatingActionButton
    private lateinit var btnPrevWeek: ImageButton
    private lateinit var btnNextWeek: ImageButton
    private lateinit var tvWeekLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvDates      = findViewById(R.id.rvDates)
        rvTasks      = findViewById(R.id.rvTasks)
        fabAddTask   = findViewById(R.id.fabAddTask)
        fabContacts  = findViewById(R.id.fabContacts)
        btnPrevWeek  = findViewById(R.id.btnPrevWeek)
        btnNextWeek  = findViewById(R.id.btnNextWeek)
        tvWeekLabel  = findViewById(R.id.tvWeekLabel)

        rvDates.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvDates.adapter = DateAdapter(generateWeekDates(weekStart)) { date ->
            selectedDate = date
            refreshWeek()
        }

        adapter = TaskAdapter { task ->
            lastViewedTask = task
            Intent(this, TaskDetailActivity::class.java).also {
                it.putExtra(TaskDetailActivity.EXTRA_TASK, task)
                it.putExtra(TaskDetailActivity.EXTRA_DATE_CONTEXT, selectedDate.toString())
                startActivityForResult(it, REQUEST_VIEW_TASK)
            }
        }
        rvTasks.layoutManager = LinearLayoutManager(this)
        rvTasks.adapter = adapter

        fabAddTask.setOnClickListener {
            startActivityForResult(
                Intent(this, ChooseActionActivity::class.java),
                REQUEST_CHOOSE_ADD
            )
        }

        fabContacts.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }

        btnPrevWeek.setOnClickListener {
            weekStart = weekStart.minusWeeks(1)
            refreshWeek()
        }
        btnNextWeek.setOnClickListener {
            weekStart = weekStart.plusWeeks(1)
            refreshWeek()
        }

        loadTasksFromFirestore()
        refreshWeek()
    }

    private fun loadTasksFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("tasks")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                allTasks.clear()
                for (doc in snapshots.documents) {
                    val title = doc.getString("title") ?: continue
                    val date  = LocalDate.parse(doc.getString("date")!!)
                    val start = doc.getString("startTime")!!
                    val end   = doc.getString("endTime")!!
                    val isDaily  = doc.getBoolean("isDaily") ?: false
                    val isWeekly = doc.getBoolean("isWeekly") ?: false
                    val recEnd   = doc.getString("recurrenceEndDate")
                        ?.let { LocalDate.parse(it) }
                    val excluded = doc.get("excludedDates") as? List<String> ?: emptyList()
                    val exclDates = excluded.map { LocalDate.parse(it) }.toMutableList()
                    val isPerm   = doc.getBoolean("isPermanent") ?: false

                    allTasks += TaskItem(
                        title             = title,
                        date              = date,
                        startTime         = start,
                        endTime           = end,
                        isDaily           = isDaily,
                        isWeekly          = isWeekly,
                        recurrenceEndDate = recEnd,
                        excludedDates     = exclDates,
                        isPermanent       = isPerm
                    )
                }
                refreshWeek()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_CHOOSE_ADD -> {
                (data.getSerializableExtra(EXTRA_NEW_TASK) as? TaskItem)?.let { task ->
                    saveTaskToFirestore(task)
                    allTasks += task
                    refreshWeek()
                    return
                }
                data.getSerializableExtra(EXTRA_GOAL)?.let { goal ->
                    startActivityForResult(
                        Intent(this, GoalSuggestionsActivity::class.java)
                            .putExtra(EXTRA_GOAL, goal)
                            .putExtra(EXTRA_ALL_TASKS, ArrayList(allTasks))
                            .putExtra(EXTRA_WEEK_START, weekStart.toString()),
                        REQUEST_GOAL_SUGGESTIONS
                    )
                }
            }
            REQUEST_GOAL_SUGGESTIONS -> {
                (data.getSerializableExtra(EXTRA_NEW_TASKS) as? List<TaskItem>)?.let { list ->
                    list.forEach { task ->
                        saveTaskToFirestore(task)
                        allTasks += task
                    }
                    refreshWeek()
                }
            }
            REQUEST_VIEW_TASK -> {
            }
        }
    }

    private fun saveTaskToFirestore(task: TaskItem) {
        val uid = auth.currentUser?.uid ?: return
        val tasksCol = firestore.collection("users")
            .document(uid)
            .collection("tasks")

        val docId = "${task.title}_${task.date}"
        val data = mutableMapOf<String, Any>(
            "title"             to task.title,
            "date"              to task.date.toString(),
            "startTime"         to task.startTime,
            "endTime"           to task.endTime,
            "isDaily"           to task.isDaily,
            "isWeekly"          to task.isWeekly,
            "recurrenceEndDate" to (task.recurrenceEndDate?.toString() ?: ""),
            "excludedDates"     to task.excludedDates.map { it.toString() },
            "isPermanent"       to task.isPermanent
        )
        if (!task.isPermanent) {
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }
            data["expiresAt"] = Timestamp(cal.time)
        }

        tasksCol.document(docId)
            .set(data)
            .addOnSuccessListener { Log.d("FS", "Saved $docId") }
            .addOnFailureListener { e -> Log.e("FS", "Save failed", e) }
    }

    private fun refreshWeek() {
        val dates = generateWeekDates(weekStart)
        (rvDates.adapter as DateAdapter).updateDates(dates)
        if (selectedDate !in dates) selectedDate = weekStart
        updateTasks()
        val month = weekStart.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        tvWeekLabel.text = "Week of ${weekStart.dayOfMonth} $month"
    }

    private fun updateTasks() {
        val list = allTasks.filter { t ->
            if (selectedDate in t.excludedDates) return@filter false
            when {
                t.date == selectedDate -> true
                t.isDaily && !selectedDate.isBefore(t.date) &&
                        t.recurrenceEndDate?.let { !selectedDate.isAfter(it) } != false -> true
                t.isWeekly && !selectedDate.isBefore(t.date) &&
                        t.recurrenceEndDate?.let { !selectedDate.isAfter(it) } != false &&
                        selectedDate.dayOfWeek == t.date.dayOfWeek -> true
                else -> false
            }
        }.sortedBy { LocalTime.parse(it.startTime, timeFmt) }

        adapter.submitList(list)
    }

    private fun generateWeekDates(center: LocalDate) =
        (0L..6L).map { center.with(DayOfWeek.SUNDAY).plusDays(it) }
}
